package com.lvai.config;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * AI 自动配置增强
 * 用于解决引入多个 AI Starter 导致的 EmbeddingModel Bean 冲突问题
 */
@Configuration
public class AiBeanConfig {

    @Bean
    @Primary
    public EmbeddingModel primaryEmbeddingModel(OpenAiEmbeddingModel openAiEmbeddingModel) {
        return openAiEmbeddingModel;
    }

}
