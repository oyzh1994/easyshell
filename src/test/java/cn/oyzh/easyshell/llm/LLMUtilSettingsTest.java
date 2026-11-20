package cn.oyzh.easyshell.llm;

import cn.oyzh.easyshell.domain.ShellSetting;
import cn.oyzh.easyshell.llm.agent.LLMAgentConcurrency;
import cn.oyzh.easyshell.llm.agent.LLMAgentFailurePolicy;
import cn.oyzh.easyshell.llm.agent.LLMAgentMode;
import cn.oyzh.easyshell.llm.agent.LLMAgentOrchestrationMode;
import cn.oyzh.easyshell.llm.agent.LLMAgentStepLimit;
import cn.oyzh.easyshell.llm.provider.LLMProvider;
import cn.oyzh.store.jdbc.Column;
import dev.langchain4j.model.anthropic.AnthropicChatRequestParameters;
import dev.langchain4j.model.chat.ChatModel;
import dev.langchain4j.model.googleai.GeminiThinkingConfig;
import dev.langchain4j.model.openai.OpenAiChatRequestParameters;
import dev.langchain4j.model.openai.OpenAiStreamingChatModel;
import org.junit.Test;

import java.util.List;
import java.lang.reflect.Field;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class LLMUtilSettingsTest {

    @Test
    public void selectedModelAndOutputLimitReachChatAndStreamingModels() {
        LLMProvider provider = provider(LLMApiType.OPENAI, "provider-default");

        ChatModel chatModel = LLMUtil.createChatModel(
                provider, "selected-model", LLMThinkingLevel.HIGH, 4096);
        OpenAiStreamingChatModel streamingModel = (OpenAiStreamingChatModel)
                LLMUtil.createStreamingChatModel(
                        provider, "selected-model", LLMThinkingLevel.HIGH, 4096);

        assertEquals("selected-model", chatModel.defaultRequestParameters().modelName());
        assertEquals(Integer.valueOf(4096), chatModel.defaultRequestParameters().maxOutputTokens());
        assertEquals("high", ((OpenAiChatRequestParameters)
                chatModel.defaultRequestParameters()).reasoningEffort());
        assertEquals("selected-model", streamingModel.defaultRequestParameters().modelName());
        assertEquals(Integer.valueOf(4096),
                streamingModel.defaultRequestParameters().maxOutputTokens());
        assertEquals("high", streamingModel.defaultRequestParameters().reasoningEffort());
    }

    @Test
    public void providerDefaultIsUsedWhenNoModelIsSelected() {
        LLMProvider provider = provider(LLMApiType.OPENAI, "provider-default");

        ChatModel model = LLMUtil.createChatModel(provider, " ", LLMThinkingLevel.OFF, 8192);

        assertEquals("provider-default", model.defaultRequestParameters().modelName());
        assertNull(((OpenAiChatRequestParameters) model.defaultRequestParameters()).reasoningEffort());
    }

    @Test
    public void modelSelectionPersistsAndFallsBackWhenProviderChanges() {
        assertEquals("saved-model", LLMUtil.selectModel(
                List.of("default-model", "saved-model"),
                null, "saved-model", "default-model"));
        assertEquals("provider-default", LLMUtil.selectModel(
                List.of("provider-default", "second"),
                "model-from-previous-provider", "saved-from-previous-provider",
                "provider-default"));
        assertEquals("first", LLMUtil.selectModel(
                List.of("first", "second"), null, null, null));
        assertNull(LLMUtil.selectModel(List.of(), "model", "model", "model"));
    }

    @Test
    public void providerThinkingLevelsUseValidProviderValues() {
        assertEquals("low", LLMUtil.openAiReasoningEffort(LLMThinkingLevel.LOW));
        assertEquals("medium", LLMUtil.openAiReasoningEffort(LLMThinkingLevel.MEDIUM));
        assertEquals("high", LLMUtil.openAiReasoningEffort(LLMThinkingLevel.MAX));
        assertEquals(GeminiThinkingConfig.GeminiThinkingLevel.LOW,
                LLMUtil.geminiThinkingLevel(LLMThinkingLevel.LOW));
        assertEquals(GeminiThinkingConfig.GeminiThinkingLevel.MEDIUM,
                LLMUtil.geminiThinkingLevel(LLMThinkingLevel.MEDIUM));
        assertEquals(GeminiThinkingConfig.GeminiThinkingLevel.HIGH,
                LLMUtil.geminiThinkingLevel(LLMThinkingLevel.XHIGH));
    }

    @Test
    public void anthropicThinkingBudgetStaysBelowOutputLimit() {
        assertEquals(4096, LLMUtil.anthropicThinkingBudget(LLMThinkingLevel.HIGH, 8192));
        assertEquals(1024, LLMUtil.anthropicThinkingBudget(LLMThinkingLevel.HIGH, 1025));
        assertEquals(0, LLMUtil.anthropicThinkingBudget(LLMThinkingLevel.HIGH, 1024));

        LLMProvider provider = provider(LLMApiType.ANTHROPIC, "claude-test");
        AnthropicChatRequestParameters parameters = (AnthropicChatRequestParameters)
                LLMUtil.createChatModel(provider, null, LLMThinkingLevel.HIGH, 8192)
                        .defaultRequestParameters();
        assertEquals("enabled", parameters.thinkingType());
        assertEquals(Integer.valueOf(4096), parameters.thinkingBudgetTokens());
    }

    @Test
    public void settingsClampAndPersistLlmParameters() {
        ShellSetting setting = new ShellSetting();
        assertEquals(8192, setting.getLlmMaxOutputTokens());
        assertEquals(20, setting.getLlmMaxMessages());

        setting.setLlmModel("  model-a  ");
        setting.setLlmMaxMessages(0);
        setting.setLlmMaxOutputTokens(1);
        setting.setLlmThinkingLevel(999);

        assertEquals("model-a", setting.getLlmModel());
        assertEquals(1, setting.getLlmMaxMessages());
        assertEquals(256, setting.getLlmMaxOutputTokens());
        assertEquals(LLMThinkingLevel.MEDIUM.ordinal(), setting.getLlmThinkingLevel());

        setting.setLlmMaxMessages(5000);
        setting.setLlmMaxOutputTokens(1_000_000);
        ShellSetting copy = new ShellSetting();
        copy.copy(setting);
        assertEquals(1000, copy.getLlmMaxMessages());
        assertEquals(131_072, copy.getLlmMaxOutputTokens());
        assertEquals("model-a", copy.getLlmModel());
    }

    @Test
    public void invalidApiTypeOrdinalUsesSafeDefault() {
        assertEquals(LLMApiType.OPENAI, LLMApiType.fromOrdinal(-1));
        assertEquals(LLMApiType.ANTHROPIC, LLMApiType.fromOrdinal(1));
        assertEquals(LLMApiType.OPENAI, LLMApiType.fromOrdinal(999));

        LLMProvider provider = new LLMProvider();
        provider.setApiType(999);
        provider.setDefaultModel("  model-a  ");
        assertEquals(LLMApiType.OPENAI.ordinal(), provider.getApiType());
        assertEquals("OPENAI", provider.getApiTypeName());
        assertEquals("model-a", provider.getDefaultModel());
    }

    @Test
    public void joinedSystemMessagesDoNotRepeatSharedRules() {
        String message = LLMUtil.joinSystemMessages(
                "session tools\n\nshared language rule\n\nshared execution rule",
                "local tools\n\nshared language rule\n\nshared execution rule",
                "mcp tools\n\nshared execution rule");

        assertEquals("session tools\n\nshared language rule\n\nshared execution rule"
                + "\n\nlocal tools\n\nmcp tools", message);
    }

    @Test
    public void modelAndOutputSettingsArePersistentColumns() throws Exception {
        Field model = ShellSetting.class.getDeclaredField("llmModel");
        Field outputTokens = ShellSetting.class.getDeclaredField("llmMaxOutputTokens");
        assertNotNull(model.getAnnotation(Column.class));
        assertNotNull(outputTokens.getAnnotation(Column.class));
    }

    @Test
    public void thinkingVisibilityIsIndependentAndPersistent() throws Exception {
        ShellSetting setting = new ShellSetting();
        assertTrue(setting.isLlmShowThinking());

        setting.setLlmThinkingLevel(LLMThinkingLevel.HIGH.ordinal());
        setting.setLlmShowThinking(false);
        ShellSetting copy = new ShellSetting();
        copy.copy(setting);

        assertFalse(copy.isLlmShowThinking());
        assertEquals(LLMThinkingLevel.HIGH.ordinal(), copy.getLlmThinkingLevel());
        Field field = ShellSetting.class.getDeclaredField("llmShowThinking");
        assertNotNull(field.getAnnotation(Column.class));
    }

    @Test
    public void agentModeDefaultsToMultiAndPersistsSingle() throws Exception {
        ShellSetting setting = new ShellSetting();
        assertEquals(LLMAgentMode.MULTI.ordinal(), setting.getLlmAgentMode());
        assertEquals(LLMAgentMode.MULTI, LLMAgentMode.fromOrdinal(999));

        setting.setLlmAgentMode(LLMAgentMode.SINGLE.ordinal());
        ShellSetting copy = new ShellSetting();
        copy.copy(setting);

        assertEquals(LLMAgentMode.SINGLE.ordinal(), copy.getLlmAgentMode());
        Field field = ShellSetting.class.getDeclaredField("llmAgentMode");
        assertNotNull(field.getAnnotation(Column.class));
    }

    @Test
    public void agentOrchestrationDefaultsAndPersists() throws Exception {
        ShellSetting setting = new ShellSetting();
        assertEquals(LLMAgentOrchestrationMode.SEQUENTIAL.ordinal(),
                setting.getLlmAgentOrchestrationMode());
        assertEquals(LLMAgentConcurrency.PHYSICAL_CORES,
                setting.getLlmAgentMaxConcurrency());
        assertEquals(10, setting.getLlmAgentMaxSteps());
        assertEquals(LLMAgentFailurePolicy.CONTINUE.ordinal(),
                setting.getLlmAgentFailurePolicy());

        setting.setLlmAgentOrchestrationMode(LLMAgentOrchestrationMode.DAG.ordinal());
        setting.setLlmAgentMaxConcurrency(8);
        setting.setLlmAgentMaxSteps(20);
        setting.setLlmAgentFailurePolicy(LLMAgentFailurePolicy.RETRY.ordinal());
        ShellSetting copy = new ShellSetting();
        copy.copy(setting);

        assertEquals(LLMAgentOrchestrationMode.DAG.ordinal(),
                copy.getLlmAgentOrchestrationMode());
        assertEquals(8, copy.getLlmAgentMaxConcurrency());
        assertEquals(20, copy.getLlmAgentMaxSteps());
        assertEquals(LLMAgentFailurePolicy.RETRY.ordinal(),
                copy.getLlmAgentFailurePolicy());
        assertNotNull(ShellSetting.class.getDeclaredField("llmAgentOrchestrationMode")
                .getAnnotation(Column.class));
        assertNotNull(ShellSetting.class.getDeclaredField("llmAgentMaxConcurrency")
                .getAnnotation(Column.class));
        assertNotNull(ShellSetting.class.getDeclaredField("llmAgentFailurePolicy")
                .getAnnotation(Column.class));
        assertNotNull(ShellSetting.class.getDeclaredField("llmAgentMaxSteps")
                .getAnnotation(Column.class));
    }

    @Test
    public void agentConcurrencyResolvesFixedPhysicalAndUnlimitedValues() {
        assertEquals(2, LLMAgentConcurrency.resolve(2, 6));
        assertEquals(3, LLMAgentConcurrency.resolve(4, 3));
        assertEquals(6, LLMAgentConcurrency.resolve(LLMAgentConcurrency.UNLIMITED, 6));
        int physical = LLMAgentConcurrency.resolve(LLMAgentConcurrency.PHYSICAL_CORES, 64);
        assertTrue(physical > 0);
        assertEquals(LLMAgentConcurrency.PHYSICAL_CORES,
                LLMAgentConcurrency.normalize(99));
        assertEquals(LLMAgentConcurrency.UNLIMITED,
                LLMAgentConcurrency.normalize(-1));
        assertEquals(LLMAgentStepLimit.DEFAULT, LLMAgentStepLimit.normalize(99));
        assertEquals(20, LLMAgentStepLimit.normalize(20));
    }

    private static LLMProvider provider(LLMApiType type, String defaultModel) {
        LLMProvider provider = new LLMProvider();
        provider.setName("test");
        provider.setBaseUrl("http://127.0.0.1:1/v1");
        provider.setSk("test-key");
        provider.setApiType(type.ordinal());
        provider.setModels(List.of(defaultModel));
        provider.setDefaultModel(defaultModel);
        return provider;
    }
}
