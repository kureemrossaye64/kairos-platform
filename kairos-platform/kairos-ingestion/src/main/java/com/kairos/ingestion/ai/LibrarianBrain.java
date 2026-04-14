package com.kairos.ingestion.ai;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

/**
 * The AI Brain that makes decisions based on text and graph context.
 */
public interface LibrarianBrain {

    @SystemMessage("""
        You are an expert Knowledge Graph Librarian. 
        Your job is to organize documents into a structured hierarchy.
        
        You will receive:
        1. A SUMMARY of a new document.
        2. A list of CURRENT NODES (Candidates) in the current graph location.
        
        You must output a JSON decision on what to do next.
        
        ACTIONS:
        - "LINK_HERE": The document belongs directly under one of the current nodes (e.g., "Invoices 2024").
        - "DRILL_DOWN": The document belongs DEEPER inside one of the current nodes (e.g., current node is "Finance", but doc is "Q3 Invoice").
        - "CREATE_TOPIC": None of the current nodes fit well. Create a new sibling topic (e.g., "Legal" if only "Finance" exists).
        - "LINK_TO_CURRENT": If the current parent node itself is the perfect place and no children fit better.
        
        JSON FORMAT:
        {
           "action": "DRILL_DOWN" | "LINK_HERE" | "CREATE_TOPIC" | "LINK_TO_CURRENT",
           "targetNodeId": "ID_OF_CHOSEN_NODE (null if CREATE_TOPIC)",
           "newTopicName": "Name of new topic (Only if CREATE_TOPIC)",
           "reasoning": "Short explanation"
        }
        """)
    @UserMessage("""
        DOCUMENT SUMMARY: 
        {{summary}}
        
        CURRENT LOCATION: {{currentLocationName}}
        
        CANDIDATE SUB-NODES:
        {{candidates}}
        """)
    String decidePlacement(@V("summary") String summary, 
                           @V("currentLocationName") String currentLocationName, 
                           @V("candidates") String candidateListString);

    @SystemMessage("You are a helpful assistant. Summarize the following document content in 3 sentences, focusing on identifying the main project, department, or topic.")
    String generateSummary(String fullText);
}