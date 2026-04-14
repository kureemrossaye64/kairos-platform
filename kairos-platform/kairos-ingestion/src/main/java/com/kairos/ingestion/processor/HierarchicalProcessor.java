package com.kairos.ingestion.processor;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Stack;
import java.util.stream.Collectors;
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
import com.kairos.ingestion.utils.TextSlicer;

import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ImageContent;
import dev.langchain4j.data.message.TextContent;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.data.segment.TextSegment;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class HierarchicalProcessor implements Processor<SourceRecord, TextSegment> {

	private final StorageService storageService;
	private final ChatLanguageModel chatModel;
	private final ObjectMapper objectMapper;

	private final TextSlicer textSlicer;

	@Override
	public Stream<TextSegment> process(Stream<SourceRecord> inputStream) {
		return inputStream.flatMap(record -> {
			log.info("Starting Advanced Hierarchical Processing for: {}", record.getSourceName());
			List<TextSegment> segments = new ArrayList<>();
			ContextHydrator contextHydrator = new ContextHydrator(); // New context per file

			try (InputStream is = storageService.download(record.getStorageUri())) {
				byte[] pdfBytes = is.readAllBytes();
				try (PDDocument document = Loader.loadPDF(pdfBytes)) {
					PDFRenderer pdfRenderer = new PDFRenderer(document);
					PDFTextStripper textStripper = new PDFTextStripper();
					textStripper.setSortByPosition(true);

					for (int i = 0; i < document.getNumberOfPages(); i++) {
						log.info("Processing Page {}/{}", i + 1, document.getNumberOfPages());

						// 1. Get Visuals & Text
						BufferedImage bim = pdfRenderer.renderImageWithDPI(i, 150, ImageType.RGB);
						String base64Image = encodeImage(bim);

						textStripper.setStartPage(i + 1);
						textStripper.setEndPage(i + 1);
						String rawPageText = textStripper.getText(document);

						// 2. AI Analysis
						HierarchicalLayout layout = analyzePageHierarchy(base64Image, rawPageText);

						// 3. Hydrate & Chunk
						for (HierarchicalLayout.Block block : layout.getBlocks()) {
							if ("HEADER".equalsIgnoreCase(block.getType())) {
								contextHydrator.updateContext(block.getLevel(), block.getContent());
							} else {
								// It's content (Text or Table)
								TextSegment segment = createHydratedSegment(block, rawPageText, contextHydrator, record,
										i + 1);
								if (segment != null)
									segments.add(segment);
							}
						}
					}
				}
			} catch (Exception e) {
				log.error("Processing failed", e);
				throw new RuntimeException(e);
			}
			return segments.stream();
		});
	}

	private HierarchicalLayout analyzePageHierarchy(String base64Image, String rawText) {
		// We prompt Gemini to act as a Document Structure Tree Builder
		String prompt = """
				You are a Document Hierarchy Engine. Analyze the image and text.
				Identify Headers (H1, H2, H3), Paragraphs, and Tables.
				For more context, the image and text provided comes from a pdf document. The goal of this is to chunk the document
				intelligently and hierarchichally to be embedded for a RAG engine

				RULES:
				1. **Headers**: Extract text and estimate hierarchy level (1=Title, 2=Section, 3=Subsection).
				2. **Text**: For standard text, provide start/end anchors from the Raw Text. These are only anchors so the start and en anchors should be short. Maximum 10 words
				3. **Tables**: Transcribe to Markdown.
				4. **Irrelevant contents**: sections like table of contents yout should return an empty block

				RAW TEXT:
				"""
				+ (rawText.length() > 5000 ? rawText.substring(0, 5000) : rawText) + """

						JSON OUTPUT FORMAT:
						{
						  "blocks": [
						    { "type": "HEADER", "level": 1, "content": "Chapter 1: Hygiene" },
						    { "type": "TEXT", "start_anchor": "...", "end_anchor": "..." },
						    { "type": "TABLE", "content": "| Col | ... |" }
						  ]
						}
						""";

		UserMessage msg = UserMessage.from(TextContent.from(prompt), ImageContent.from(base64Image, "image/jpeg"));

		try {
			AiMessage response = chatModel.getModel().chat(msg).aiMessage();
			String json = cleanJson(response.text());
			return objectMapper.readValue(json, HierarchicalLayout.class);
		} catch (Exception e) {
			log.error("AI Analysis failed", e);
			return new HierarchicalLayout();
		}
	}

	private TextSegment createHydratedSegment(HierarchicalLayout.Block block, String rawText, ContextHydrator context,
			SourceRecord record, int page) {
		String content = "";

		if ("TABLE".equalsIgnoreCase(block.getType())) {
			content = block.getContent();
		} else {
			// Robust slicing logic (reused from previous step)
			content = sliceTextRobust(rawText, block.getStart_anchor(), block.getEnd_anchor(), 0);
		}

		if (content.isBlank())
			return null;

		// --- THE MAGIC: HYDRATION ---
		String hierarchyContext = context.getCurrentContextString();
		// We prepend the context to the text so the Embedding Model sees it!
		// "Context: Hygiene > Handwashing. Content: Scrub for 20 seconds."
		String hydratedContent = String.format("Context: %s\nContent: %s", hierarchyContext, content);

		// --- THE MAGIC: SYNTHETIC QUESTIONS (Hypothetical Question Indexing) ---
		// We ask the AI to generate a question this chunk answers.
		// This is costly (1 extra LLM call per chunk), but since ingestion is one-off,
		// it's worth it.
		// String syntheticQuestion = generateSyntheticQuestion(hydratedContent);

		Metadata metadata = new Metadata(record.getMetadataManifest());
		metadata.put("source_filename", record.getSourceName());
		metadata.put("page_number", String.valueOf(page));
		metadata.put("hierarchy_context", hierarchyContext); // Store separately for UI display
		// metadata.put("synthetic_question", syntheticQuestion); // Crucial for Search

		// We store the Hydrated Content in the vector store
		return TextSegment.from(hydratedContent, metadata);
	}

	private String cleanJson(String text) {
		if (text.contains("```json")) {
			return text.substring(text.indexOf("```json") + 7, text.lastIndexOf("```"));
		}
		return text;
	}

	private String encodeImage(BufferedImage image) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ImageIO.write(image, "jpg", baos);
		return Base64.getEncoder().encodeToString(baos.toByteArray());
	}

	// Include the fuzzy slicing logic from previous answer here...
	/**
	 * The Magic Method: Robust Slicing using Fuzzy Matching. Finds the start anchor
	 * and end anchor in the big string and extracts everything between.
	 */
	private String sliceTextRobust(String fullText, String startAnchor, String endAnchor, int fromIndex) {
		return textSlicer.slice(fullText, startAnchor, endAnchor, fromIndex);
	}

	@Data
	public static class HierarchicalLayout {
		private List<Block> blocks = new ArrayList<HierarchicalProcessor.HierarchicalLayout.Block>();

		@Data
		public static class Block {
			private String type; // "HEADER", "TEXT", "TABLE"
			private int level; // 1 for H1, 2 for H2 (Only valid if type=HEADER)
			private String content; // The text content or table markdown

			// Anchors for fuzzy finding in raw text
			private String start_anchor;
			private String end_anchor;
		}
	}

	public static class ContextHydrator {

		private final Stack<HeaderNode> headerStack = new Stack<>();

		private static class HeaderNode {
			int level;
			String text;

			public HeaderNode(int level, String text) {
				this.level = level;
				this.text = text;
			}
		}

		public void updateContext(int level, String headerText) {
			// If we see an H2, we pop everything H2 and below from the stack
			while (!headerStack.isEmpty() && headerStack.peek().level >= level) {
				headerStack.pop();
			}
			headerStack.push(new HeaderNode(level, headerText));
		}

		public String getCurrentContextString() {
			if (headerStack.isEmpty())
				return "";
			return headerStack.stream().map(h -> h.text).collect(Collectors.joining(" > "));
		}
	}
}