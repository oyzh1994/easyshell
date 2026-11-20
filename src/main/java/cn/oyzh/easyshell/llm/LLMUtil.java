package cn.oyzh.easyshell.llm;

import cn.oyzh.common.file.FileUtil;
import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.util.ResourceUtil;
import cn.oyzh.easyshell.llm.provider.LLMProvider;
import cn.oyzh.easyshell.store.ShellSettingStore;
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
import dev.langchain4j.http.client.HttpClientBuilder;
import dev.langchain4j.http.client.HttpClientBuilderLoader;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class LLMUtil {

    /**
     * Default maximum number of output tokens per model response.
     */
    public static final int DEFAULT_MAX_OUTPUT_TOKENS =
            LLMParameterUtil.DEFAULT_MAX_OUTPUT_TOKENS;

    /**
     * 大模型聊天连接超时时间。
     */
    public static final Duration DEFAULT_CONNECT_TIMEOUT = Duration.ofSeconds(15);

    /**
     * 大模型文本资源缓存
     */
    private static final Map<String, String> LLM_TEXT_CACHE = new ConcurrentHashMap<>();

    private LLMUtil() {
    }

    public static ChatModel createChatModel(LLMProvider provider, LLMThinkingLevel thinkingLevel) {
        return createChatModel(provider, null, thinkingLevel, DEFAULT_MAX_OUTPUT_TOKENS);
    }

    public static ChatModel createChatModel(LLMProvider provider,
                                            String modelName,
                                            LLMThinkingLevel thinkingLevel,
                                            int maxOutputTokens) {
        return createChatModel(provider, modelName, thinkingLevel, maxOutputTokens,
                configuredRequestTimeout());
    }

    public static ChatModel createChatModel(LLMProvider provider,
                                            String modelName,
                                            LLMThinkingLevel thinkingLevel,
                                            int maxOutputTokens,
                                            Duration requestTimeout) {
        validateProvider(provider);
        String model = resolveModel(provider, modelName);
        LLMThinkingLevel level = normalizeLevel(thinkingLevel);
        int maxTokens = normalizeMaxOutputTokens(maxOutputTokens);
        return switch (LLMApiType.fromOrdinal(provider.getApiType())) {
            case OPENAI -> {
                OpenAiChatModel.OpenAiChatModelBuilder builder =
                        new OpenAiChatModel.OpenAiChatModelBuilder()
                                .apiKey(provider.getSk())
                                .modelName(model)
                                .baseUrl(provider.getBaseUrl())
                                .httpClientBuilder(createHttpClientBuilder(requestTimeout))
                                .maxTokens(maxTokens);
                applyOpenAiThinking(builder, level);
                yield new OpenAiChatModel(builder);
            }
            case ANTHROPIC -> {
                AnthropicChatModel.AnthropicChatModelBuilder builder =
                        new AnthropicChatModel.AnthropicChatModelBuilder()
                                .apiKey(provider.getSk())
                                .modelName(model)
                                .baseUrl(provider.getBaseUrl())
                                .httpClientBuilder(createHttpClientBuilder(requestTimeout))
                                .maxTokens(maxTokens);
                applyAnthropicThinking(builder, level, maxTokens);
                yield new AnthropicChatModel(builder);
            }
            case GEMINI -> {
                GoogleAiGeminiChatModel.GoogleAiGeminiChatModelBuilder builder =
                        GoogleAiGeminiChatModel.builder()
                                .apiKey(provider.getSk())
                                .modelName(model)
                                .baseUrl(provider.getBaseUrl())
                                .httpClientBuilder(createHttpClientBuilder(requestTimeout))
                                .maxOutputTokens(maxTokens);
                applyGeminiThinking(builder, level);
                yield new GoogleAiGeminiChatModel(builder);
            }
            case OLLAMA -> {
                OllamaChatModel.OllamaChatModelBuilder builder =
                        new OllamaChatModel.OllamaChatModelBuilder()
                                .modelName(model)
                                .baseUrl(provider.getBaseUrl())
                                .httpClientBuilder(createHttpClientBuilder(requestTimeout))
                                .numPredict(maxTokens);
                if (level != LLMThinkingLevel.OFF) {
                    builder.returnThinking(true);
                }
                yield new OllamaChatModel(builder);
            }
        };
    }

    public static StreamingChatModel createStreamingChatModel(LLMProvider provider,
                                                              LLMThinkingLevel thinkingLevel) {
        return createStreamingChatModel(provider, null, thinkingLevel, DEFAULT_MAX_OUTPUT_TOKENS);
    }

    public static StreamingChatModel createStreamingChatModel(LLMProvider provider,
                                                              String modelName,
                                                              LLMThinkingLevel thinkingLevel,
                                                              int maxOutputTokens) {
        return createStreamingChatModel(provider, modelName, thinkingLevel,
                maxOutputTokens, configuredRequestTimeout());
    }

    public static StreamingChatModel createStreamingChatModel(LLMProvider provider,
                                                              String modelName,
                                                              LLMThinkingLevel thinkingLevel,
                                                              int maxOutputTokens,
                                                              Duration requestTimeout) {
        validateProvider(provider);
        String model = resolveModel(provider, modelName);
        LLMThinkingLevel level = normalizeLevel(thinkingLevel);
        int maxTokens = normalizeMaxOutputTokens(maxOutputTokens);
        return switch (LLMApiType.fromOrdinal(provider.getApiType())) {
            case OPENAI -> {
                OpenAiStreamingChatModel.OpenAiStreamingChatModelBuilder builder =
                        new OpenAiStreamingChatModel.OpenAiStreamingChatModelBuilder()
                                .apiKey(provider.getSk())
                                .modelName(model)
                                .baseUrl(provider.getBaseUrl())
                                .httpClientBuilder(createHttpClientBuilder(requestTimeout))
                                .maxTokens(maxTokens);
                applyOpenAiThinking(builder, level);
                yield new OpenAiStreamingChatModel(builder);
            }
            case ANTHROPIC -> {
                AnthropicStreamingChatModel.AnthropicStreamingChatModelBuilder builder =
                        new AnthropicStreamingChatModel.AnthropicStreamingChatModelBuilder()
                                .apiKey(provider.getSk())
                                .modelName(model)
                                .baseUrl(provider.getBaseUrl())
                                .httpClientBuilder(createHttpClientBuilder(requestTimeout))
                                .maxTokens(maxTokens);
                applyAnthropicThinking(builder, level, maxTokens);
                yield new AnthropicStreamingChatModel(builder);
            }
            case GEMINI -> {
                GoogleAiGeminiStreamingChatModel.GoogleAiGeminiStreamingChatModelBuilder builder =
                        GoogleAiGeminiStreamingChatModel.builder()
                                .apiKey(provider.getSk())
                                .modelName(model)
                                .baseUrl(provider.getBaseUrl())
                                .httpClientBuilder(createHttpClientBuilder(requestTimeout))
                                .maxOutputTokens(maxTokens);
                applyGeminiThinking(builder, level);
                yield new GoogleAiGeminiStreamingChatModel(builder);
            }
            case OLLAMA -> {
                OllamaStreamingChatModel.OllamaStreamingChatModelBuilder builder =
                        new OllamaStreamingChatModel.OllamaStreamingChatModelBuilder()
                                .modelName(model)
                                .baseUrl(provider.getBaseUrl())
                                .httpClientBuilder(createHttpClientBuilder(requestTimeout))
                                .numPredict(maxTokens);
                if (level != LLMThinkingLevel.OFF) {
                    builder.returnThinking(true);
                }
                yield new OllamaStreamingChatModel(builder);
            }
        };
    }

    /**
     * @deprecated use {@link #createStreamingChatModel(LLMProvider, LLMThinkingLevel)}.
     */
    @Deprecated
    public static StreamingChatModel crateStreamingChatModel(LLMProvider provider,
                                                              LLMThinkingLevel thinkingLevel) {
        return createStreamingChatModel(provider, thinkingLevel);
    }

    public static String resolveModel(LLMProvider provider, String modelName) {
        validateProvider(provider);
        String model = modelName == null || modelName.isBlank()
                ? provider.getDefaultModel()
                : modelName.trim();
        if (model == null || model.isBlank()) {
            throw new IllegalArgumentException("LLM model name is required.");
        }
        return model;
    }

    /**
     * Selects a model without retaining a stale selection after switching providers.
     *
     * @param available       models offered by the current provider
     * @param current         current UI selection
     * @param preferred       persisted model selection
     * @param providerDefault provider default model
     * @return selected model, or {@code null} when the provider has no models
     */
    public static String selectModel(List<String> available,
                                     String current,
                                     String preferred,
                                     String providerDefault) {
        if (available == null || available.isEmpty()) {
            return null;
        }
        if (current != null && available.contains(current)) {
            return current;
        }
        if (preferred != null && available.contains(preferred)) {
            return preferred;
        }
        if (providerDefault != null && available.contains(providerDefault)) {
            return providerDefault;
        }
        return available.getFirst();
    }

    public static int normalizeMaxOutputTokens(int value) {
        return LLMParameterUtil.normalizeMaxOutputTokens(value);
    }

    public static LLMThinkingLevel normalizeLevel(LLMThinkingLevel level) {
        return level == null ? LLMThinkingLevel.MEDIUM : level;
    }

    /**
     * Joins system prompt sections while removing exact duplicate paragraphs.
     * Tool groups append shared language and execution rules independently, so
     * combining their prompts would otherwise repeat those large sections.
     *
     * @param messages system prompt sections
     * @return joined prompt
     */
    public static String joinSystemMessages(String... messages) {
        if (messages == null || messages.length == 0) {
            return "";
        }
        Set<String> paragraphs = new LinkedHashSet<>();
        for (String message : messages) {
            if (message == null || message.isBlank()) {
                continue;
            }
            for (String paragraph : message.strip().split("\\R\\s*\\R")) {
                String normalized = paragraph.strip();
                if (!normalized.isEmpty()) {
                    paragraphs.add(normalized);
                }
            }
        }
        return String.join("\n\n", List.copyOf(paragraphs));
    }

    private static void applyOpenAiThinking(OpenAiChatModel.OpenAiChatModelBuilder builder,
                                            LLMThinkingLevel level) {
        if (level != LLMThinkingLevel.OFF) {
            builder.returnThinking(true)
                    .reasoningEffort(openAiReasoningEffort(level));
        }
    }

    private static void applyOpenAiThinking(OpenAiStreamingChatModel.OpenAiStreamingChatModelBuilder builder,
                                            LLMThinkingLevel level) {
        if (level != LLMThinkingLevel.OFF) {
            builder.returnThinking(true)
                    .reasoningEffort(openAiReasoningEffort(level));
        }
    }

    private static void applyAnthropicThinking(AnthropicChatModel.AnthropicChatModelBuilder builder,
                                               LLMThinkingLevel level,
                                               int maxOutputTokens) {
        if (level != LLMThinkingLevel.OFF) {
            int thinkingBudget = anthropicThinkingBudget(level, maxOutputTokens);
            if (thinkingBudget <= 0) {
                return;
            }
            builder.returnThinking(true)
                    .thinkingType("enabled")
                    .thinkingBudgetTokens(thinkingBudget)
                    .thinkingDisplay("summarized");
        }
    }

    private static void applyAnthropicThinking(AnthropicStreamingChatModel.AnthropicStreamingChatModelBuilder builder,
                                               LLMThinkingLevel level,
                                               int maxOutputTokens) {
        if (level != LLMThinkingLevel.OFF) {
            int thinkingBudget = anthropicThinkingBudget(level, maxOutputTokens);
            if (thinkingBudget <= 0) {
                return;
            }
            builder.returnThinking(true)
                    .thinkingType("enabled")
                    .thinkingBudgetTokens(thinkingBudget)
                    .thinkingDisplay("summarized");
        }
    }

    private static void applyGeminiThinking(GoogleAiGeminiChatModel.GoogleAiGeminiChatModelBuilder builder,
                                            LLMThinkingLevel level) {
        if (level != LLMThinkingLevel.OFF) {
            GeminiThinkingConfig thinkingConfig = GeminiThinkingConfig.builder()
                    .includeThoughts(true)
                    .thinkingLevel(geminiThinkingLevel(level))
                    .build();
            builder.returnThinking(true)
                    .thinkingConfig(thinkingConfig);
        }
    }

    private static void applyGeminiThinking(
            GoogleAiGeminiStreamingChatModel.GoogleAiGeminiStreamingChatModelBuilder builder,
            LLMThinkingLevel level) {
        if (level != LLMThinkingLevel.OFF) {
            GeminiThinkingConfig thinkingConfig = GeminiThinkingConfig.builder()
                    .includeThoughts(true)
                    .thinkingLevel(geminiThinkingLevel(level))
                    .build();
            builder.returnThinking(true)
                    .thinkingConfig(thinkingConfig);
        }
    }

    static String openAiReasoningEffort(LLMThinkingLevel level) {
        return switch (normalizeLevel(level)) {
            case OFF -> "low";
            case LOW -> "low";
            case MEDIUM -> "medium";
            case HIGH, XHIGH, MAX -> "high";
        };
    }

    static int anthropicThinkingBudget(LLMThinkingLevel level) {
        return anthropicThinkingBudget(level, DEFAULT_MAX_OUTPUT_TOKENS);
    }

    static int anthropicThinkingBudget(LLMThinkingLevel level, int maxOutputTokens) {
        int normalizedMaxTokens = normalizeMaxOutputTokens(maxOutputTokens);
        if (normalizedMaxTokens <= 1024) {
            return 0;
        }
        int requestedBudget = switch (normalizeLevel(level)) {
            case OFF, LOW -> 1024;
            case MEDIUM -> 2048;
            case HIGH -> 4096;
            case XHIGH -> 6144;
            case MAX -> 7168;
        };
        return Math.min(requestedBudget, normalizedMaxTokens - 1);
    }

    static GeminiThinkingConfig.GeminiThinkingLevel geminiThinkingLevel(LLMThinkingLevel level) {
        return switch (normalizeLevel(level)) {
            case OFF, LOW -> GeminiThinkingConfig.GeminiThinkingLevel.LOW;
            case MEDIUM -> GeminiThinkingConfig.GeminiThinkingLevel.MEDIUM;
            case HIGH, XHIGH, MAX -> GeminiThinkingConfig.GeminiThinkingLevel.HIGH;
        };
    }

    private static void validateProvider(LLMProvider provider) {
        if (provider == null) {
            throw new IllegalArgumentException("LLM provider is required.");
        }
    }

    private static HttpClientBuilder createHttpClientBuilder(Duration requestTimeout) {
        HttpClientBuilder builder = HttpClientBuilderLoader.loadHttpClientBuilder()
                .connectTimeout(DEFAULT_CONNECT_TIMEOUT);
        return requestTimeout == null ? builder : builder.readTimeout(requestTimeout);
    }

    private static Duration configuredRequestTimeout() {
        return LLMRequestTimeout.fromMinutes(
                ShellSettingStore.SETTING.getLlmTimeoutMinutes()).getDuration();
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
