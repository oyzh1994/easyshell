package cn.oyzh.easyshell.llm.capability;

import cn.oyzh.easyshell.llm.LLMApiType;
import cn.oyzh.easyshell.llm.LLMModelContextWindowUtil;
import cn.oyzh.easyshell.llm.provider.LLMProvider;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class LLMModelCapabilityResolverTest {

    @Test
    public void builtInDefaultsUse256KContext() {
        LLMModelCapability capability = LLMModelCapabilityResolver.builtIn(
                provider(LLMApiType.OPENAI), "custom-model");

        assertEquals(LLMModelContextWindowUtil.DEFAULT_TOKENS,
                capability.getContextWindowTokens());
        assertTrue(capability.isTools());
        assertFalse(capability.isImage());
        assertTrue(capability.isThinking());
    }

    @Test
    public void builtInCapabilitiesCoverKnownProviderFamilies() {
        LLMModelCapability openAi = LLMModelCapabilityResolver.builtIn(
                provider(LLMApiType.OPENAI), "o3-mini");
        assertTrue(openAi.isImage());
        assertTrue(openAi.isThinking());

        LLMModelCapability gemini = LLMModelCapabilityResolver.builtIn(
                provider(LLMApiType.GEMINI), "gemini-2.5-pro");
        assertTrue(gemini.isImage());
        assertTrue(gemini.isPdf());
        assertTrue(gemini.isAudio());
        assertTrue(gemini.isVideo());
        assertTrue(gemini.isThinking());
    }

    @Test
    public void embeddingModelsDoNotExposeToolCalling() {
        LLMModelCapability capability = LLMModelCapabilityResolver.builtIn(
                provider(LLMApiType.OPENAI), "text-embedding-3-small");
        assertFalse(capability.isTools());
        assertFalse(capability.isThinking());
    }

    private static LLMProvider provider(LLMApiType type) {
        LLMProvider provider = new LLMProvider();
        provider.setUid("provider-1");
        provider.setApiType(type.ordinal());
        return provider;
    }
}
