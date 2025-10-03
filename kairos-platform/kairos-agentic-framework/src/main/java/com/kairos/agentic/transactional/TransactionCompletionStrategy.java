package com.kairos.agentic.transactional;

/**
 * Strategy interface for defining the final action to be taken
 * once a TransactionContext has been successfully filled.
 */
public interface TransactionCompletionStrategy {
    /**
     * The name of the transaction this strategy handles.
     * Must match the name in the @TransactionalEntity annotation.
     */
    String getTransactionName();

    /**
     * Executes the final business logic using the fully populated context.
     * @param context The completed TransactionContext.
     * @return A user-facing success or failure message.
     */
    String execute(TransactionContext context);
}