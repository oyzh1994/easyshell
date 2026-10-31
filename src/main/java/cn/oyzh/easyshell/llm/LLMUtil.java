package cn.oyzh.easyshell.llm;

import dev.langchain4j.model.anthropic.AnthropicStreamingChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.ollama.OllamaStreamingChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;

public class LLMUtil {

    public static StreamingChatModel crateStreamingChatModel(LLMProvider provider) {
        if (provider.getApiType() == 0) {
            OpenAiStreamingChatModel.OpenAiStreamingChatModelBuilder builder = new OpenAiStreamingChatModel.OpenAiStreamingChatModelBuilder()
                    .apiKey(provider.getSk())
                    .modelName(provider.getDefaultModel())
                    .baseUrl(provider.getBaseUrl());
            return new OpenAiStreamingChatModel(builder);
        }
        if (provider.getApiType() == 2) {
            AnthropicStreamingChatModel.AnthropicStreamingChatModelBuilder builder = new AnthropicStreamingChatModel.AnthropicStreamingChatModelBuilder()
                    .apiKey(provider.getSk())
                    .modelName(provider.getDefaultModel())
                    .baseUrl(provider.getBaseUrl());
            return new AnthropicStreamingChatModel(builder);
        }
        OllamaStreamingChatModel.OllamaStreamingChatModelBuilder builder = new OllamaStreamingChatModel.OllamaStreamingChatModelBuilder()
                .modelName(provider.getDefaultModel())
                .baseUrl(provider.getBaseUrl());
        return new OllamaStreamingChatModel(builder);
    }
}
