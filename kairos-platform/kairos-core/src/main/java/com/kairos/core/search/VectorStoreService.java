package com.kairos.core.search;

import java.util.List;

/**
 * High-level interface for interacting with the vector store.
 * This abstracts the underlying implementation (e.g., PgVector, Chroma, Pinecone).
 */
public interface VectorStoreService {

    /**
     * Adds a single document to the vector store.
     * The document's content will be converted to an embedding and stored.
     * @param document The document to add.
     */
    void addDocument(VdbDocument document);

    /**
     * Adds a list of documents to the vector store in a batch.
     * @param documents The list of documents to add.
     */
    void addDocuments(List<VdbDocument> documents);

    /**
     * Finds the most semantically similar documents to a given query text.
     * @param queryText The text to search for.
     * @param topK The maximum number of similar documents to return.
     * @return A list of VdbDocument objects representing the most relevant results.
     */
    //List<VdbDocument> findRelevant(String queryText, int topK);
    
    //public List<VdbDocument> findHybrid(String queryText, int topK, Class<?> entityClass);
    
    //public void createIndexWithMapping(Class<?> entityClass)throws IOException;
    
    //void index(UUID documentId, Object document);
    
    //void delete(Class<?> entityClass, UUID documentId);
    
    //public void deleteAll(Class<?> entityClass, List<UUID> documentIds);
    
    // --- Unified Search Method ---
    //SearchResult search(SearchQuery query, Class<?> entityClass);
    
    //public SearchResult vectorSearch(SearchQuery query, Class<?> entityClass, int neighbours);
}