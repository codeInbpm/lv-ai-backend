package com.lvai;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("com.lvai.mapper")
@SpringBootApplication(exclude = {
        // 禁用 Azure OpenAI (解决当前的 Endpoint 报错)
        org.springframework.ai.model.azure.openai.autoconfigure.AzureOpenAiChatAutoConfiguration.class,
        // 禁用 Anthropic (解决之前的 simpleApiKey 报错)
        org.springframework.ai.model.anthropic.autoconfigure.AnthropicChatAutoConfiguration.class,
        // 如果之后报 Bedrock/Ollama 的错，继续往这里加
})
public class LvAiApplication {
    public static void main(String[] args) {
        SpringApplication.run(LvAiApplication.class, args);
    }
}
