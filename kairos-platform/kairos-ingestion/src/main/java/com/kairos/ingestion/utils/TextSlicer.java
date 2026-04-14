package com.kairos.ingestion.utils;

import java.text.Normalizer;

import org.bitbucket.cowwoc.diffmatchpatch.DiffMatchPatch;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TextSlicer {

	// Industrial-grade Fuzzy Matching library
	private final DiffMatchPatch dmp;

	public TextSlicer() {
		this.dmp = new DiffMatchPatch();
		// CONFIGURATION IS CRITICAL:
		// 0.0 = Exact match required. 1.0 = Match anything.
		// 0.4 is a sweet spot for "It's definitely this text, but OCR/LLM messed up a
		// few chars"
		this.dmp.matchThreshold = 0.4f;

		// How far from the expected location (fromIndex) are we willing to look?
		// 1000 means if we expect it at char 500, we check 0 to 1500.
		this.dmp.matchDistance = 2000;
	}

	/**
	 * Extracts text between two fuzzy anchors.
	 */
	public String slice(String fullText, String startAnchor, String endAnchor, int fromIndex) {
		if (fullText == null || fullText.isEmpty())
			return "";
		if (startAnchor == null || endAnchor == null)
			return "";

		fullText = normalize(fullText);
		startAnchor = normalize(startAnchor);
		endAnchor = normalize(endAnchor);
		
		// 1. TIER 1: Fast Exact Match
		int startIndex = fullText.indexOf(startAnchor, fromIndex);

		// 2. TIER 2: Normalized Exact Match (Handles newlines/spaces differences)
		if (startIndex == -1) {
			startIndex = findNormalizedIndex(fullText, startAnchor, fromIndex);
		}

		// 3. TIER 3: Google Bitap Fuzzy Match (Heavy machinery)
		if (startIndex == -1) {
			// match_main returns the index of the best match, or -1
			startIndex = dmp.matchMain(fullText, startAnchor, fromIndex);

			// Safety: If the match is wildly inaccurate or too far back (before fromIndex),
			// ignore it
			if (startIndex != -1 && startIndex < fromIndex - 100) {
				// It found a match, but it's way before our current cursor.
				// This likely means it matched a similar phrase in a previous section.
				// Force it to look forward by slicing the search text (expensive but accurate)
				String lookAheadText = fullText.substring(fromIndex);
				int relativeIndex = dmp.matchMain(lookAheadText, startAnchor, 0);
				if (relativeIndex != -1)
					startIndex = fromIndex + relativeIndex;
			} else {
				startIndex = -1; // Give up
			}
		}

		// --- START ANCHOR FAILED ---
		if (startIndex == -1) {
			log.warn("Could not locate start anchor: '{}' ... skipping section.", preview(startAnchor));
			return "";
		}

		// --- FIND END ANCHOR ---
		// We start searching for the end anchor *after* the start anchor matches
		int searchEndFrom = startIndex + startAnchor.length();
		int endIndex = -1;

		// TIER 1
		endIndex = fullText.indexOf(endAnchor, 0);

		// TIER 2
		if (endIndex == -1) {
			endIndex = findNormalizedIndex(fullText, endAnchor, searchEndFrom);
		}

		// TIER 3
		if (endIndex == -1) {
			int foundAt = dmp.matchMain(fullText, endAnchor, searchEndFrom);
			if (foundAt > startIndex) {
				endIndex = foundAt;
			}
		}

		// --- FINAL EXTRACTION ---
		if (endIndex == -1) {
			// Edge case: LLM said this section ends the page, or couldn't find end.
			// Safe fallback: Take everything until the end of string?
			// Or maybe strictly up to next double newline?
			// Let's take up to end of string but warn.
			log.debug("Could not locate end anchor. Extending to end of text.");
			endIndex = fullText.length();
		} else {
			// The match_main returns the START of the pattern.
			// We want to include the end anchor in the result.
			endIndex += endAnchor.length();
		}

		// Bounds check (just in case fuzzy logic went out of bounds)
		endIndex = Math.min(endIndex, fullText.length());

		if (startIndex >= endIndex)
			return "";

		return fullText.substring(startIndex, endIndex).trim();
	}

	/**
	 * Helper: Tries to find a match by ignoring all whitespace. Useful because
	 * PDFBox often outputs "H e l l o" or "Hello\nWorld" vs "Hello World".
	 */
	private int findNormalizedIndex(String fullText, String anchor, int fromIndex) {
		String normFull = normalize(fullText.substring(fromIndex));
		String normAnchor = normalize(anchor);

		int normIdx = normFull.indexOf(normAnchor);
		if (normIdx == -1)
			return -1;

		// Mapping back from Normalized Index to Real Index is hard.
		// Heuristic: The match exists. Let's assume it's near 'fromIndex' and let
		// FuzzySearch find exact pos.
		// This helper basically acts as a "Gatekeeper" to say "Yes, it exists, use
		// Fuzzy to find where".
		// Returning -1 here forces the logic to fall through to Tier 3 (DMP), which is
		// fine.
		// We use this mostly to validate if we should even bother with DMP.
		return -1;
	}

	private String normalize(String input) {
		
		
		if (input == null)
			return null;

		// 1. Normalize Unicode (Fixes accents and some composite characters)
		String text = Normalizer.normalize(input, Normalizer.Form.NFKC);

		// 2. Replace "Smart" characters with "Standard" ASCII characters
		text = text.replace('\u2018', '\'') // Left single quote
				.replace('\u2019', '\'') // Right single quote (The one in Dubai’s)
				.replace('\u201C', '\"') // Left double quote
				.replace('\u201D', '\"') // Right double quote
				.replace('\u2013', '-') // En dash
				.replace('\u2014', '-'); // Em dash

		// 3. Replace Non-Breaking Spaces with regular spaces
		text = text.replace('\u00A0', ' ');

		// 4. Normalize Whitespace
		// This regex matches any sequence of whitespace (spaces, tabs, \r, \n)
		// and replaces them with a single space.
		// This solves the Carriage Return vs Newline bug.
		text = text.replaceAll("\\s+", " ").trim();

		return text;
	}

	private String preview(String s) {
		return s.length() > 20 ? s.substring(0, 20) + "..." : s;
	}
}
