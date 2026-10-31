package cn.oyzh.easyshell.llm;

import cn.oyzh.common.file.FileUtil;
import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.util.ResourceUtil;
import dev.langchain4j.model.anthropic.AnthropicChatModel;
import dev.langchain4j.model.anthropic.AnthropicStreamingChatModel;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.model.googleai.GeminiThinkingConfig;
import dev.langchain4j.model.googleai.GoogleAiGeminiChatModel;
import dev.langchain4j.model.googleai.GoogleAiGeminiStreamingChatModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import dev.langchain4j.model.ollama.OllamaStreamingChatModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LLMUtil {

    /**
     * 大模型文本资源缓存
     */
    private static final Map<String, String> LLM_TEXT_CACHE = new ConcurrentHashMap<>();

    public static ChatModel createChatModel(LLMProvider provider, LLMThinkingLevel thinkingLevel) {
        if (provider.getApiType() == 0) {
            OpenAiChatModel.OpenAiChatModelBuilder builder = new OpenAiChatModel.OpenAiChatModelBuilder()
                    .apiKey(provider.getSk())
                    .modelName(provider.getDefaultModel())
                    .baseUrl(provider.getBaseUrl());
            if (thinkingLevel != LLMThinkingLevel.OFF) {
                builder.returnThinking(true)
                        .reasoningEffort(thinkingLevel.name().toLowerCase());
            }
            return new OpenAiChatModel(builder);
        }
        if (provider.getApiType() == 1) {
            AnthropicChatModel.AnthropicChatModelBuilder builder = new AnthropicChatModel.AnthropicChatModelBuilder()
                    .apiKey(provider.getSk())
                    .modelName(provider.getDefaultModel())
                    .baseUrl(provider.getBaseUrl());
            if (thinkingLevel != LLMThinkingLevel.OFF) {
                builder.returnThinking(true)
                        .thinkingType("enabled");
            }
            return new AnthropicChatModel(builder);
        }
        if (provider.getApiType() == 2) {
            GoogleAiGeminiChatModel.GoogleAiGeminiChatModelBuilder builder = GoogleAiGeminiChatModel.builder();
            builder.apiKey(provider.getSk())
                    .modelName(provider.getDefaultModel())
                    .baseUrl(provider.getBaseUrl());
            if (thinkingLevel != LLMThinkingLevel.OFF) {
                GeminiThinkingConfig thinkingConfig = GeminiThinkingConfig.builder()
                        .includeThoughts(true)
                        .thinkingLevel(thinkingLevel.name().toLowerCase())
                        .build();
                builder.returnThinking(true)
                        .thinkingConfig(thinkingConfig);
            }
            return new GoogleAiGeminiChatModel(builder);
        }
        OllamaChatModel.OllamaChatModelBuilder builder = new OllamaChatModel.OllamaChatModelBuilder()
                .modelName(provider.getDefaultModel())
                .baseUrl(provider.getBaseUrl());
        if (thinkingLevel != LLMThinkingLevel.OFF) {
            builder.returnThinking(true);
        }
        return new OllamaChatModel(builder);
    }

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
        if (provider.getApiType() == 1) {
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
        if (provider.getApiType() == 2) {
            GoogleAiGeminiStreamingChatModel.GoogleAiGeminiStreamingChatModelBuilder builder = GoogleAiGeminiStreamingChatModel.builder();
            builder.apiKey(provider.getSk())
                    .modelName(provider.getDefaultModel())
                    .baseUrl(provider.getBaseUrl());
            if (thinkingLevel != LLMThinkingLevel.OFF) {
                GeminiThinkingConfig thinkingConfig = GeminiThinkingConfig.builder()
                        .includeThoughts(true)
                        .thinkingLevel(thinkingLevel.name().toLowerCase())
                        //.thinkingBudget(4096)
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

    /**
     * 获取执行模式系统提示词
     *
     * @param executionMode 执行模式
     * @return 系统提示词
     */
    public static String executionSystemMessage(LLMExecutionMode executionMode) {
        LLMExecutionMode mode = executionMode == null ? LLMExecutionMode.APPROVE : executionMode;
        String file = switch (mode) {
            case SUGGEST -> "execution_mode_suggest.txt";
            case APPROVE -> "execution_mode_approve.txt";
            case UNRESTRICTED -> "execution_mode_unrestricted.txt";
        };
        return readLlmText(file);
    }

    /**
     * 获取语言规则提示词
     *
     * @return 语言规则提示词
     */
    public static String languageSystemMessage() {
        return readLlmText("language_rule.txt");
    }

    /**
     * 读取大模型文本资源
     *
     * @param fileName 文件名
     * @return 文本内容
     */
    public static String readLlmText(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            return "";
        }
        return LLM_TEXT_CACHE.computeIfAbsent(fileName, LLMUtil::loadLlmText);
    }

    private static String loadLlmText(String fileName) {
        String path = "llm/" + fileName;
        try (InputStream stream = ResourceUtil.getResourceAsStream(path)) {
            if (stream == null) {
                JulLog.warn("llm text not found: {}", path);
                return "";
            }
            return FileUtil.readString(stream, StandardCharsets.UTF_8);
        } catch (Exception ex) {
            JulLog.error("read llm text error: " + path, ex);
            return "";
        }
    }
}
