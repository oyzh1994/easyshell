package cn.oyzh.easyshell.llm;

import dev.langchain4j.model.anthropic.AnthropicStreamingChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.googleai.GeminiThinkingConfig;
import dev.langchain4j.model.googleai.GoogleAiGeminiStreamingChatModel;
import dev.langchain4j.model.ollama.OllamaStreamingChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;

public class LLMUtil {

    public static StreamingChatModel crateStreamingChatModel(LLMProvider provider, LLMThinkingLevel thinkingLevel) {
        if (provider.getApiType() == 0) {
            OpenAiStreamingChatModel.OpenAiStreamingChatModelBuilder builder = new OpenAiStreamingChatModel.OpenAiStreamingChatModelBuilder()
                    .apiKey(provider.getSk())
                    .modelName(provider.getDefaultModel())
                    .baseUrl(provider.getBaseUrl());
            if (thinkingLevel != LLMThinkingLevel.OFF) {
                builder = builder.returnThinking(true)
                        .reasoningEffort(thinkingLevel.name().toLowerCase());
            }
            return new OpenAiStreamingChatModel(builder);
        }
        if (provider.getApiType() == 2) {
            AnthropicStreamingChatModel.AnthropicStreamingChatModelBuilder builder = new AnthropicStreamingChatModel.AnthropicStreamingChatModelBuilder()
                    .apiKey(provider.getSk())
                    .modelName(provider.getDefaultModel())
                    .baseUrl(provider.getBaseUrl());
            if (thinkingLevel != LLMThinkingLevel.OFF) {
                builder = builder.returnThinking(true)
                        .thinkingType("enabled");
            }
            return new AnthropicStreamingChatModel(builder);
        }
        if (provider.getApiType() == 3) {
            GoogleAiGeminiStreamingChatModel.GoogleAiGeminiStreamingChatModelBuilder builder = GoogleAiGeminiStreamingChatModel.builder();
            builder.apiKey(provider.getSk())
                    .modelName(provider.getDefaultModel())
                    .baseUrl(provider.getBaseUrl());
            if (thinkingLevel != LLMThinkingLevel.OFF) {
                GeminiThinkingConfig thinkingConfig = GeminiThinkingConfig.builder()
                        .includeThoughts(true)
                        .thinkingLevel(thinkingLevel.name().toLowerCase())
                        .thinkingBudget(4096)
                        .build();
                builder = builder.returnThinking(true)
                        .thinkingConfig(thinkingConfig);
            }
            return new GoogleAiGeminiStreamingChatModel(builder);
        }
        OllamaStreamingChatModel.OllamaStreamingChatModelBuilder builder = new OllamaStreamingChatModel.OllamaStreamingChatModelBuilder()
                .modelName(provider.getDefaultModel())
                .baseUrl(provider.getBaseUrl());
        if (thinkingLevel != LLMThinkingLevel.OFF) {
            builder = builder.returnThinking(true);
        }
        return new OllamaStreamingChatModel(builder);
    }
}
