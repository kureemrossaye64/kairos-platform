package com.kairos.autoconfigure;

import com.kairos.agentic.rag.QueryExpansionService;
import com.kairos.agentic.rag.ResultRankingService;
import com.kairos.core.ai.ChatLanguageModel;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@ConditionalOnClass(QueryExpansionService.class)
@ConditionalOnProperty(prefix = "kairos.ai", name = "enabled", havingValue = "true", matchIfMissing = true)
public class KairosAgenticAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public QueryExpansionService queryExpansionService(@Qualifier("cost-effective") ChatLanguageModel chatModel) {
        // We inject the 'cost-effective' model we configured in KairosAIAutoConfiguration
        return new QueryExpansionService(chatModel);
    }

    @Bean
    @ConditionalOnMissingBean
    public ResultRankingService resultRankingService(@Qualifier("cost-effective") ChatLanguageModel chatModel) {
        // We inject the 'cost-effective' model (re-ranking is cheaper with flash models)
        return new ResultRankingService(chatModel); // Ensure your service constructor accepts this
    }
}