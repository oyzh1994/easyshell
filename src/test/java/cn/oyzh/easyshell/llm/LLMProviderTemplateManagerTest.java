package cn.oyzh.easyshell.llm;

import org.junit.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class LLMProviderTemplateManagerTest {

    @Test
    public void registersCommonProviderTemplates() {
        List<LLMProviderTemplate> templates = LLMProviderTemplateManager.INSTANCE.getTemplates();

        assertTrue(templates.size() >= 85);
        Set<String> ids = new HashSet<>();
        Set<String> endpoints = new HashSet<>();
        for (LLMProviderTemplate template : templates) {
            assertTrue(ids.add(template.getId()));
            assertFalse(template.getBaseUrl().isBlank());
            assertNotNull(template.getApiType());
            assertTrue(endpoints.add(template.getBaseUrl() + "|" + template.getApiType()));
        }

        this.assertTemplate("openai", "https://api.openai.com/v1", LLMApiType.OPENAI);
        this.assertTemplate("gemini", "https://generativelanguage.googleapis.com", LLMApiType.GEMINI);
        this.assertTemplate("deepseek-openai", "https://api.deepseek.com", LLMApiType.OPENAI);
        this.assertTemplate("deepseek-anthropic", "https://api.deepseek.com/anthropic/v1", LLMApiType.ANTHROPIC);
        this.assertTemplate("moonshot-cn-anthropic", "https://api.moonshot.cn/anthropic/v1", LLMApiType.ANTHROPIC);
        this.assertTemplate("moonshot-global-anthropic", "https://api.moonshot.ai/anthropic/v1", LLMApiType.ANTHROPIC);
        this.assertTemplate("zhipu-cn-anthropic", "https://open.bigmodel.cn/api/anthropic/v1", LLMApiType.ANTHROPIC);
        this.assertTemplate("zai-anthropic", "https://api.z.ai/api/anthropic/v1", LLMApiType.ANTHROPIC);
        this.assertTemplate("qwen-cn-openai", "https://dashscope.aliyuncs.com/compatible-mode/v1", LLMApiType.OPENAI);
        this.assertTemplate("qwen-global-openai", "https://dashscope-intl.aliyuncs.com/compatible-mode/v1", LLMApiType.OPENAI);
        this.assertTemplate("minimax-cn-anthropic", "https://api.minimax.cn/anthropic/v1", LLMApiType.ANTHROPIC);
        this.assertTemplate("xiaomi-mimo-openai", "https://api.xiaomimimo.com/v1", LLMApiType.OPENAI);
        this.assertTemplate("xiaomi-mimo-anthropic", "https://api.xiaomimimo.com/anthropic/v1", LLMApiType.ANTHROPIC);
        this.assertTemplate("xiaomi-mimo-token-plan-openai", "https://token-plan-cn.xiaomimimo.com/v1", LLMApiType.OPENAI);
        this.assertTemplate("xiaomi-mimo-token-plan-anthropic", "https://token-plan-cn.xiaomimimo.com/anthropic/v1", LLMApiType.ANTHROPIC);
        this.assertTemplate("longcat", "https://api.longcat.chat/openai", LLMApiType.OPENAI);
        this.assertTemplate("modelscope", "https://api-inference.modelscope.cn/v1", LLMApiType.OPENAI);
        this.assertTemplate("ppio", "https://api.ppinfra.com/v3/openai", LLMApiType.OPENAI);
        this.assertTemplate("huggingface", "https://router.huggingface.co/v1", LLMApiType.OPENAI);
        this.assertTemplate("github-models", "https://models.github.ai/inference", LLMApiType.OPENAI);
    }

    @Test
    public void matchesTemplateByAddressAndApiType() {
        LLMProviderTemplate openAi = LLMProviderTemplateManager.INSTANCE.findMatching(
                "https://api.deepseek.com/", LLMApiType.OPENAI);
        LLMProviderTemplate anthropic = LLMProviderTemplateManager.INSTANCE.findMatching(
                "https://api.deepseek.com/anthropic/v1/", LLMApiType.ANTHROPIC);

        assertEquals("deepseek-openai", openAi.getId());
        assertEquals("deepseek-anthropic", anthropic.getId());
        assertNull(LLMProviderTemplateManager.INSTANCE.findMatching(
                "https://api.deepseek.com", LLMApiType.ANTHROPIC));
    }

    @Test
    public void displaysApiTypeForDualProtocolProviders() {
        LLMProviderTemplate deepSeek = LLMProviderTemplateManager.INSTANCE.getTemplate("deepseek-anthropic");
        LLMProviderTemplate openAi = LLMProviderTemplateManager.INSTANCE.getTemplate("openai");

        assertEquals("DeepSeek · ANTHROPIC", deepSeek.getDisplayName());
        assertEquals("OpenAI", openAi.getDisplayName());
    }

    @Test
    public void searchesTemplatesByNameAndAddress() {
        assertTrue(LLMProviderTemplateManager.INSTANCE.search("MiMo").stream()
                .anyMatch(template -> template.getId().equals("xiaomi-mimo-anthropic")));
        assertTrue(LLMProviderTemplateManager.INSTANCE.search("moonshot.ai").stream()
                .anyMatch(template -> template.getId().equals("moonshot-global-anthropic")));
        assertTrue(LLMProviderTemplateManager.INSTANCE.search("Token Plan").stream()
                .anyMatch(template -> template.getId().equals("xiaomi-mimo-token-plan-anthropic")));
        assertFalse(LLMProviderTemplateManager.INSTANCE.search("deepseek").stream()
                .anyMatch(template -> template.getId().startsWith("xiaomi")));
        assertEquals(LLMProviderTemplateManager.INSTANCE.getTemplates().size(),
                LLMProviderTemplateManager.INSTANCE.search("  ").size());
    }

    private void assertTemplate(String id, String baseUrl, LLMApiType apiType) {
        LLMProviderTemplate template = LLMProviderTemplateManager.INSTANCE.getTemplate(id);
        assertNotNull(id, template);
        assertEquals(baseUrl, template.getBaseUrl());
        assertEquals(apiType, template.getApiType());
    }
}
