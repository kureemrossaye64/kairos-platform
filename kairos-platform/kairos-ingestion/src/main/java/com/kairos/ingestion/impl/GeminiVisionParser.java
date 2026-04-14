package com.kairos.ingestion.impl;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Stack;

import javax.imageio.ImageIO;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kairos.core.ai.ChatLanguageModel; // Your wrapper or LangChain4j generic
import com.kairos.ingestion.model.DocumentNode;
import com.kairos.ingestion.model.DocumentNode.NodeType;
import com.kairos.ingestion.model.DocumentTree;
import com.kairos.ingestion.spi.DocumentParser;
import com.kairos.ingestion.utils.TextSlicer; // Your existing utility

import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ImageContent;
import dev.langchain4j.data.message.TextContent;
import dev.langchain4j.data.message.UserMessage;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class GeminiVisionParser implements DocumentParser {

    private final ChatLanguageModel chatModel;
    private final ObjectMapper objectMapper;
    private final TextSlicer textSlicer; // Keeps your robust slicing logic

    @Override
    public DocumentTree parse(InputStream inputStream, String fileName) {
        log.info("Starting AI-driven parsing for: {}", fileName);
        DocumentTree tree = new DocumentTree(fileName);
        
        // Stack to maintain hierarchy state across pages
        // We push the ROOT node initially
        Stack<DocumentNode> contextStack = new Stack<>();
        contextStack.push(tree.getRootNode());

        try {
            byte[] pdfBytes = inputStream.readAllBytes();
            try (PDDocument document = Loader.loadPDF(pdfBytes)) {
                PDFRenderer pdfRenderer = new PDFRenderer(document);
                PDFTextStripper textStripper = new PDFTextStripper();
                textStripper.setSortByPosition(true);

                for (int i = 0; i < document.getNumberOfPages(); i++) {
                    log.info("Parsing Page {}/{}", i + 1, document.getNumberOfPages());
                    
                    // 1. Render Image (for Vision)
                    BufferedImage bim = pdfRenderer.renderImageWithDPI(i, 150, ImageType.RGB);
                    String base64Image = encodeImage(bim);

                    // 2. Extract Raw Text (for Anchoring)
                    textStripper.setStartPage(i + 1);
                    textStripper.setEndPage(i + 1);
                    String rawPageText = textStripper.getText(document);

                    // 3. AI Extraction
                    AiPageLayout layout = analyzePageWithGemini(base64Image, rawPageText);

                    // 4. Update Tree Structure
                    processLayoutIntoTree(layout, rawPageText, contextStack, i + 1);
                }
            }
        } catch (IOException e) {
            log.error("Failed to parse PDF", e);
            throw new RuntimeException("Document parsing failed", e);
        }

        return tree;
    }

    private void processLayoutIntoTree(AiPageLayout layout, String rawPageText, Stack<DocumentNode> stack, int pageNum) {
        if (layout == null || layout.getBlocks() == null) return;

        for (AiPageLayout.Block block : layout.getBlocks()) {
            
            // Determine Node Type
            NodeType type = NodeType.CONTENT;
            if ("HEADER".equalsIgnoreCase(block.getType())) type = NodeType.SECTION;
            else if ("TABLE".equalsIgnoreCase(block.getType())) type = NodeType.TABLE;

            // Extract Content (Slice or Markdown)
            String content = block.getContent();
            if (type == NodeType.CONTENT && block.getStart_anchor() != null) {
                // Use your existing robust slicer
                content = textSlicer.slice(rawPageText, block.getStart_anchor(), block.getEnd_anchor(), 0);
            }
            if (content == null || content.isBlank()) continue;

            // --- TREE LOGIC ---
            if (type == NodeType.SECTION) {
                int newLevel = block.getLevel();
                
                // Pop stack until we find a parent with level < newLevel
                // (Root is level 0, H1 is 1. If we see H2 (2), we keep H1. If we see H1 (1), we pop H2 and H1)
                while (stack.peek().getLevel() >= newLevel && stack.peek().getLevel() != 0) {
                    stack.pop();
                }

                DocumentNode newSection = new DocumentNode(NodeType.SECTION, content, newLevel);
                newSection.getMetadata().put("page", pageNum);
                
                // Add to current parent
                stack.peek().addChild(newSection);
                
                // Push this section as the new active context
                stack.push(newSection);

            } else {
                // It's a leaf node (Content/Table) -> Add to currently active section
                DocumentNode leaf = new DocumentNode(type, content, 0); // content doesn't affect stack level
                leaf.getMetadata().put("page", pageNum);
                stack.peek().addChild(leaf);
            }
        }
    }

    private AiPageLayout analyzePageWithGemini(String base64Image, String rawText) {
        String prompt = """
                Analyze this document page image.
                Extract the layout into a JSON structure.
                
                Identify:
                1. HEADERS: Extract text and estimate hierarchy level (1=Title/H1, 2=H2, 3=H3).
                2. TEXT BLOCKS: Do NOT transcribe full text. Provide only the first 5 words (start_anchor) and last 5 words (end_anchor).
                3. TABLES: Transcribe to Markdown.
                
                Ignore page numbers or running headers/footers.
                
                RAW TEXT HINT (Use this for correct spelling):
                """ + (rawText.length() > 1000 ? rawText.substring(0, 1000) + "..." : rawText) + """
                
                OUTPUT JSON FORMAT:
                {
                  "blocks": [
                    { "type": "HEADER", "level": 1, "content": "Introduction" },
                    { "type": "TEXT", "start_anchor": "The market has shifted...", "end_anchor": "...to new paradigms." },
                    { "type": "TABLE", "content": "| ID | Value |..." }
                  ]
                }
                """;

        UserMessage msg = UserMessage.from(
                TextContent.from(prompt),
                ImageContent.from(base64Image, "image/jpeg")
        );

        try {
            AiMessage response = chatModel.getModel().chat(msg).aiMessage(); // Assuming LangChain4j API
            String json = cleanJson(response.text());
            return objectMapper.readValue(json, AiPageLayout.class);
        } catch (Exception e) {
            log.error("Gemini analysis failed", e);
            return new AiPageLayout(); // Return empty to skip page rather than crash
        }
    }

    private String encodeImage(BufferedImage image) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "jpg", baos);
        return Base64.getEncoder().encodeToString(baos.toByteArray());
    }

    private String cleanJson(String text) {
        text = text.trim();
        if (text.startsWith("```json")) {
            text = text.substring(7);
        }
        if (text.startsWith("```")) { // Handle case where language isn't specified
             text = text.substring(3);
        }
        if (text.endsWith("```")) {
            text = text.substring(0, text.length() - 3);
        }
        return text.trim();
    }

    // --- DTO for AI Response ---
    @Data
    public static class AiPageLayout {
        private List<Block> blocks = new ArrayList<>();

        @Data
        public static class Block {
            private String type; // HEADER, TEXT, TABLE
            private int level;
            private String content;
            private String start_anchor;
            private String end_anchor;
        }
    }
}