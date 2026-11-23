package cn.oyzh.easyshell.llm.model;

import cn.oyzh.easyshell.llm.LLMModelContextWindowUtil;
import cn.oyzh.easyshell.llm.capability.LLMModelCapability;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * 自定义模型与模型选项测试。
 */
public class LLMModelChoiceTest {

    @Test
    public void customModelConvertsToCapability() {
        LLMCustomModel model = new LLMCustomModel();
        model.setUid("uid-1");
        model.setName("My Model");
        model.setModelId("my-model-id");
        model.setContextWindowTokens(128 * 1024);
        model.setTools(true);
        model.setImage(true);
        model.setPdf(false);
        model.setAudio(false);
        model.setVideo(false);
        model.setThinking(false);

        LLMModelChoice choice = LLMModelChoice.custom(model);
        LLMModelCapability capability = choice.capability(null);

        assertEquals("My Model", choice.getName());
        assertEquals("my-model-id", choice.getModelId());
        assertEquals("custom:uid-1", choice.getKey());
        assertTrue(choice.isCustom());
        assertEquals(128 * 1024, capability.getContextWindowTokens());
        assertTrue(capability.isTools());
        assertTrue(capability.isImage());
        assertFalse(capability.isThinking());
    }

    @Test
    public void providerModelKeepsIdAsKeyAndDisplayName() {
        LLMModelChoice choice = LLMModelChoice.provider("gpt-4o");

        assertEquals("gpt-4o", choice.getName());
        assertEquals("gpt-4o", choice.getModelId());
        assertEquals("gpt-4o", choice.getKey());
        assertFalse(choice.isCustom());
    }

    @Test
    public void contextWindowUsesSupportedPreset() {
        LLMCustomModel model = new LLMCustomModel();
        model.setContextWindowTokens(999);

        assertEquals(LLMModelContextWindowUtil.DEFAULT_TOKENS,
                model.getContextWindowTokens());
        assertEquals("256K", model.getContextWindowText());
    }
}
