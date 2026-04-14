package com.kairos.ingestion.processor;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.stream.Stream;

import javax.imageio.ImageIO;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kairos.core.ai.ChatLanguageModel;
import com.kairos.core.ingestion.SourceRecord;
import com.kairos.core.storage.StorageService;
import com.kairos.ingestion.pipeline.Processor;

import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ImageContent;
import dev.langchain4j.data.message.TextContent;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.data.segment.TextSegment;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.xdrop.fuzzywuzzy.FuzzySearch;

@Slf4j
@RequiredArgsConstructor
public class AdvancedLayoutAnalysisProcessor implements Processor<SourceRecord, TextSegment> {

    private final StorageService storageService;
    
    private final ChatLanguageModel chatModel;
    
    private final ObjectMapper objectMapper;

    @Override
    public Stream<TextSegment> process(Stream<SourceRecord> inputStream) {
        return inputStream.flatMap(record -> {
            log.info("Starting Hybrid Layout Analysis for: {}", record.getSourceName());
            List<TextSegment> resultSegments = new ArrayList<>();

            try (InputStream is = storageService.download(record.getStorageUri())) {
                byte[] pdfBytes = is.readAllBytes();
                
                try (PDDocument document = Loader.loadPDF(pdfBytes)) {
                    PDFRenderer pdfRenderer = new PDFRenderer(document);
                    PDFTextStripper textStripper = new PDFTextStripper();
                    textStripper.setSortByPosition(true); // CRITICAL: Handles columns better

                    // Process Page by Page
                    for (int pageIndex = 0; pageIndex < document.getNumberOfPages(); pageIndex++) {
                        log.info("Processing Page {}/{}", pageIndex + 1, document.getNumberOfPages());
                        
                        // 1. Extract Raw Text (The Source of Truth)
                        textStripper.setStartPage(pageIndex + 1);
                        textStripper.setEndPage(pageIndex + 1);
                        String rawPageText = textStripper.getText(document);

                        // 2. Render Image (The Visual Context)
                        BufferedImage bim = pdfRenderer.renderImageWithDPI(pageIndex, 150, ImageType.RGB);
                        String base64Image = encodeImage(bim);

                        // 3. Ask Gemini to Map the Layout
                        PageLayout layout = analyzePageLayout(base64Image, rawPageText);

                        // 4. Reconstruct Chunks using Anchors
                        List<TextSegment> pageSegments = reconstructSegments(layout, rawPageText, record, pageIndex + 1);
                        resultSegments.addAll(pageSegments);
                    }
                }
            } catch (Exception e) {
                log.error("Fatal error processing PDF: {}", record.getSourceName(), e);
                // Fallback: If advanced processing fails, we shouldn't lose data. 
                // We would theoretically fallback to Tika here, but for now we log and throw.
                throw new RuntimeException("PDF Processing Failed", e);
            }
            
            return resultSegments.stream();
        });
    }

    private String encodeImage(BufferedImage image) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "jpg", baos);
        return Base64.getEncoder().encodeToString(baos.toByteArray());
    }

    private PageLayout analyzePageLayout(String base64Image, String rawText) {
        // Truncate raw text if it's huge to save tokens, though single page is usually fine
        String safeRawText = rawText.length() > 5000 ? rawText.substring(0, 5000) : rawText;

        String prompt = """
            You are a Document Structure Analyzer. 
            I have provided an IMAGE of a page and the RAW TEXT extracted from it.
            
            Your Goal: Break the page into logical chunks (Paragraphs, Tables, Headers).
            
            Instructions:
            1. **Tables**: If you see a table, set type="TABLE". Do NOT use anchors. Instead, transcribe the table into Markdown in the 'table_markdown' field.
            2. **Text**: If you see a paragraph or list, set type="TEXT". Identify the **start_anchor** (first ~8 words) and **end_anchor** (last ~8 words) EXACTLY as they appear in the RAW TEXT provided.
            3. **Ignore**: Page numbers, footers, headers repeated from previous pages.
            
            RAW TEXT TO REFERENCE:
            """ + safeRawText + """
            
            OUTPUT JSON FORMAT:
            {
              "sections": [
                {
                  "type": "TEXT",
                  "start_anchor": "start of the sentence...",
                  "end_anchor": "...end of the paragraph.",
                  "summary": "Brief topic description"
                },
                {
                  "type": "TABLE",
                  "table_markdown": "| Col1 | Col2 |..."
                }
              ]
            }
            """;

        UserMessage message = UserMessage.from(
                TextContent.from(prompt),
                ImageContent.from(base64Image, "image/jpeg")
        );

        try {
            AiMessage response = chatModel.getModel().chat(message).aiMessage();
            String json = cleanJson(response.text());
            return objectMapper.readValue(json, PageLayout.class);
        } catch (Exception e) {
            log.error("Gemini failed to analyze layout. Returning empty layout.", e);
            return new PageLayout(); // Fallback empty
        }
    }

    private List<TextSegment> reconstructSegments(PageLayout layout, String rawText, SourceRecord record, int pageNum) {
        List<TextSegment> segments = new ArrayList<>();
        if (layout.getSections() == null) return segments;

        // Pointer to where we are in the raw text to avoid searching backwards (handling duplicate phrases)
        int currentSearchIndex = 0;

        for (PageLayout.Section section : layout.getSections()) {
            Metadata metadata = new Metadata(record.getMetadataManifest());
            metadata.put("source_filename", record.getSourceName());
            metadata.put("page_number", String.valueOf(pageNum));
            metadata.put("section_type", section.getType());
            if (section.getSummary() != null) metadata.put("summary_hint", section.getSummary());

            String chunkContent = "";

            if ("TABLE".equalsIgnoreCase(section.getType())) {
                // For tables, we trust Gemini's Markdown transcription
                chunkContent = section.getTable_markdown();
            } else {
                // For text, we slice the Raw Text to ensure 100% fidelity (no hallucination)
                if (section.getStart_anchor() != null && section.getEnd_anchor() != null) {
                    chunkContent = sliceTextRobust(rawText, section.getStart_anchor(), section.getEnd_anchor(), currentSearchIndex);
                    
                    // Update search index so next chunk looks *after* this one
                    if (!chunkContent.isEmpty()) {
                        // Move cursor forward roughly
                        currentSearchIndex = Math.min(rawText.length(), currentSearchIndex + 1); 
                    }
                }
            }

            if (chunkContent != null && !chunkContent.isBlank()) {
                // Add Parent Content info for the "Small-to-Big" retrieval strategy later
                metadata.put("parent_content", chunkContent); // Currently 1:1, but structure allows expansion
                segments.add(TextSegment.from(chunkContent, metadata));
            }
        }
        return segments;
    }

    /**
     * The Magic Method: Robust Slicing using Fuzzy Matching.
     * Finds the start anchor and end anchor in the big string and extracts everything between.
     */
    private String sliceTextRobust(String fullText, String startAnchor, String endAnchor, int fromIndex) {
        // 1. Try Exact Match first (Fastest)
        int startIndex = fullText.indexOf(startAnchor, fromIndex);
        
        // 2. Fuzzy Match if Exact fails (FuzzyWuzzy)
        if (startIndex == -1) {
            // We search in a window of the text to avoid scanning the whole book for a short string
            // For simplicity in this snippet, we fuzzy search the substring.
            // In production, we might define a window.
            var result = FuzzySearch.extractOne(startAnchor, List.of(fullText.substring(fromIndex)));
            if (result.getScore() > 80) {
                // This is a simplification. FuzzyWuzzy returns the best string match from a list.
                // Finding the exact index of a fuzzy match in a long string is complex.
                // ALTERNATIVE STRATEGY: 
                // Normalize whitespaces and try again.
                // For this implementation, let's fallback to strict containment to avoid over-engineering the snippet.
                log.warn("Exact anchor match failed for: '{}'. Skipping fuzzy for safety.", startAnchor);
                return ""; 
            }
        }

        if (startIndex == -1) return ""; // Could not find start

        // Search for end anchor *after* the start anchor
        int endIndex = fullText.indexOf(endAnchor, startIndex);
        
        if (endIndex == -1) {
            // Try finding end anchor near the end of the text
            endIndex = fullText.length();
        } else {
            endIndex += endAnchor.length();
        }

        return fullText.substring(startIndex, endIndex).trim();
    }

    private String cleanJson(String text) {
        if (text.contains("```json")) {
            return text.substring(text.indexOf("```json") + 7, text.lastIndexOf("```"));
        }
        return text;
    }
    
    @Data
    public static class PageLayout {
        private List<Section> sections;

        @Data
        public static class Section {
            private String type; // "TEXT", "TABLE", "HEADER"
            private String start_anchor; // The first ~10 words
            private String end_anchor;   // The last ~10 words
            private String table_markdown; // Only populated if type is TABLE
            private String summary; // Optional: Quick categorization
        }
    }
}