package com.lvai.config;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.boot.web.client.ClientHttpRequestFactories;
import org.springframework.boot.web.client.ClientHttpRequestFactorySettings;
import org.springframework.boot.web.client.RestClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.time.Duration;

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

    @Bean
    public RestClientCustomizer restClientCustomizer() {
        return restClientBuilder -> restClientBuilder
                .requestFactory(ClientHttpRequestFactories.get(
                        ClientHttpRequestFactorySettings.DEFAULTS
                                .withConnectTimeout(Duration.ofSeconds(30))   // 连接超时
                                .withReadTimeout(Duration.ofSeconds(180))     // 读取超时（建议至少 3 分钟）
                ));
    }

}
