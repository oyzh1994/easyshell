package cn.oyzh.easyshell.llm;

import cn.oyzh.common.util.StringUtil;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Registry for built-in LLM provider templates.
 *
 * @author oyzh
 * @since 2026-10-31
 */
public final class LLMProviderTemplateManager {

    public static final LLMProviderTemplateManager INSTANCE = new LLMProviderTemplateManager();

    private final Map<String, LLMProviderTemplate> templates = new LinkedHashMap<>();

    private LLMProviderTemplateManager() {
        this.registerBuiltIns();
    }

    public void register(LLMProviderTemplate template) {
        if (template == null) {
            throw new IllegalArgumentException("Template cannot be null");
        }
        if (this.templates.putIfAbsent(template.getId(), template) != null) {
            throw new IllegalArgumentException("Duplicate template id: " + template.getId());
        }
    }

    public void register(String id, String name, String baseUrl, LLMApiType apiType) {
        this.register(new LLMProviderTemplate(id, name, baseUrl, apiType));
    }

    public List<LLMProviderTemplate> getTemplates() {
        return List.copyOf(this.templates.values());
    }

    public LLMProviderTemplate getTemplate(String id) {
        return id == null ? null : this.templates.get(id);
    }

    public LLMProviderTemplate findMatching(String baseUrl, LLMApiType apiType) {
        for (LLMProviderTemplate template : this.templates.values()) {
            if (template.matches(baseUrl, apiType)) {
                return template;
            }
        }
        return null;
    }

    /**
     * Search templates by provider name or API address.
     *
     * @param keyword search keyword
     * @return matching templates
     */
    public List<LLMProviderTemplate> search(String keyword) {
        if (StringUtil.isBlank(keyword)) {
            return this.getTemplates();
        }
        String value = keyword.trim();
        return this.templates.values().stream()
                .filter(template -> StringUtil.containsIgnoreCase(template.getName(), value)
                        || StringUtil.containsIgnoreCase(template.getBaseUrl(), value))
                .toList();
    }

    private void registerBuiltIns() {
        // International model labs and direct APIs.
        this.register("openai", "OpenAI", "https://api.openai.com/v1", LLMApiType.OPENAI);
        this.register("anthropic", "Anthropic", "https://api.anthropic.com/v1", LLMApiType.ANTHROPIC);
        this.register("gemini", "Google Gemini", "https://generativelanguage.googleapis.com", LLMApiType.GEMINI);
        this.register("mistral", "Mistral AI", "https://api.mistral.ai/v1", LLMApiType.OPENAI);
        this.register("xai", "xAI Grok", "https://api.x.ai/v1", LLMApiType.OPENAI);
        this.register("cohere", "Cohere", "https://api.cohere.ai/compatibility/v1", LLMApiType.OPENAI);
        this.register("perplexity", "Perplexity", "https://api.perplexity.ai", LLMApiType.OPENAI);
        this.register("ai21", "AI21 Labs", "https://api.ai21.com/studio/v1", LLMApiType.OPENAI);
        this.register("meta", "Meta Llama", "https://api.meta.ai/v1", LLMApiType.OPENAI);
        this.register("upstage", "Upstage Solar", "https://api.upstage.ai/v1/solar", LLMApiType.OPENAI);
        this.register("sakana", "Sakana AI", "https://api.sakana.ai/v1", LLMApiType.OPENAI);
        this.register("moonshot-global-openai", "Moonshot Kimi (Global)", "https://api.moonshot.ai/v1", LLMApiType.OPENAI);
        this.register("moonshot-global-anthropic", "Moonshot Kimi (Global)", "https://api.moonshot.ai/anthropic/v1", LLMApiType.ANTHROPIC);
        this.register("minimax-global-openai", "MiniMax (Global)", "https://api.minimax.io/v1", LLMApiType.OPENAI);
        this.register("minimax-global-anthropic", "MiniMax (Global)", "https://api.minimax.io/anthropic/v1", LLMApiType.ANTHROPIC);
        this.register("zai-openai", "Z.AI (Global)", "https://api.z.ai/api/paas/v4", LLMApiType.OPENAI);
        this.register("zai-anthropic", "Z.AI (Global)", "https://api.z.ai/api/anthropic/v1", LLMApiType.ANTHROPIC);
        this.register("qwen-global-openai", "Alibaba Qwen (Global)", "https://dashscope-intl.aliyuncs.com/compatible-mode/v1", LLMApiType.OPENAI);
        this.register("stepfun-global", "StepFun (Global)", "https://api.stepfun.ai/v1", LLMApiType.OPENAI);
        this.register("siliconflow-global", "SiliconFlow (Global)", "https://api.siliconflow.com/v1", LLMApiType.OPENAI);
        this.register("xiaomi-mimo-openai", "Xiaomi MiMo", "https://api.xiaomimimo.com/v1", LLMApiType.OPENAI);
        this.register("xiaomi-mimo-anthropic", "Xiaomi MiMo", "https://api.xiaomimimo.com/anthropic/v1", LLMApiType.ANTHROPIC);
        this.register("xiaomi-mimo-token-plan-openai", "Xiaomi MiMo Token Plan", "https://token-plan-cn.xiaomimimo.com/v1", LLMApiType.OPENAI);
        this.register("xiaomi-mimo-token-plan-anthropic", "Xiaomi MiMo Token Plan", "https://token-plan-cn.xiaomimimo.com/anthropic/v1", LLMApiType.ANTHROPIC);
        this.register("ollama-cloud", "Ollama Cloud", "https://ollama.com/v1", LLMApiType.OPENAI);

        // International inference platforms and aggregators.
        this.register("openrouter", "OpenRouter", "https://openrouter.ai/api/v1", LLMApiType.OPENAI);
        this.register("together", "Together AI", "https://api.together.xyz/v1", LLMApiType.OPENAI);
        this.register("fireworks", "Fireworks AI", "https://api.fireworks.ai/inference/v1", LLMApiType.OPENAI);
        this.register("groq", "Groq", "https://api.groq.com/openai/v1", LLMApiType.OPENAI);
        this.register("cerebras", "Cerebras", "https://api.cerebras.ai/v1", LLMApiType.OPENAI);
        this.register("nvidia", "NVIDIA NIM", "https://integrate.api.nvidia.com/v1", LLMApiType.OPENAI);
        this.register("huggingface", "Hugging Face", "https://router.huggingface.co/v1", LLMApiType.OPENAI);
        this.register("deepinfra", "DeepInfra", "https://api.deepinfra.com/v1/openai", LLMApiType.OPENAI);
        this.register("hyperbolic", "Hyperbolic", "https://api.hyperbolic.xyz/v1", LLMApiType.OPENAI);
        this.register("sambanova", "SambaNova", "https://api.sambanova.ai/v1", LLMApiType.OPENAI);
        this.register("nebius", "Nebius Token Factory", "https://api.tokenfactory.nebius.com/v1", LLMApiType.OPENAI);
        this.register("novita", "Novita AI", "https://api.novita.ai/openai", LLMApiType.OPENAI);
        this.register("featherless", "Featherless AI", "https://api.featherless.ai/v1", LLMApiType.OPENAI);
        this.register("chutes", "Chutes", "https://llm.chutes.ai/v1", LLMApiType.OPENAI);
        this.register("venice", "Venice AI", "https://api.venice.ai/api/v1", LLMApiType.OPENAI);
        this.register("poe", "Poe", "https://api.poe.com/v1", LLMApiType.OPENAI);
        this.register("vultr", "Vultr Inference", "https://api.vultrinference.com/v1", LLMApiType.OPENAI);
        this.register("friendli", "FriendliAI", "https://api.friendli.ai/serverless/v1", LLMApiType.OPENAI);
        this.register("scaleway", "Scaleway", "https://api.scaleway.ai/v1", LLMApiType.OPENAI);
        this.register("crusoe", "Crusoe", "https://api.inference.crusoecloud.com/v1", LLMApiType.OPENAI);
        this.register("digitalocean", "DigitalOcean Gradient", "https://inference.do-ai.run/v1", LLMApiType.OPENAI);
        this.register("coreweave", "CoreWeave Inference", "https://api.inference.wandb.ai/v1", LLMApiType.OPENAI);
        this.register("clarifai", "Clarifai", "https://api.clarifai.com/v2/ext/openai/v1", LLMApiType.OPENAI);
        this.register("aimlapi", "AI/ML API", "https://api.aimlapi.com/v1", LLMApiType.OPENAI);
        this.register("302ai", "302.AI", "https://api.302.ai/v1", LLMApiType.OPENAI);
        this.register("zenmux", "ZenMux", "https://zenmux.ai/api/v1", LLMApiType.OPENAI);
        this.register("requesty", "Requesty", "https://router.requesty.ai/v1", LLMApiType.OPENAI);
        this.register("github-models", "GitHub Models", "https://models.github.ai/inference", LLMApiType.OPENAI);
        this.register("baseten", "Baseten", "https://inference.baseten.co/v1", LLMApiType.OPENAI);
        this.register("gmicloud", "GMI Cloud", "https://api.gmi-serving.com/v1", LLMApiType.OPENAI);
        this.register("ionet", "IO.NET", "https://api.intelligence.io.solutions/api/v1", LLMApiType.OPENAI);
        this.register("vercel", "Vercel AI Gateway", "https://ai-gateway.vercel.sh/v1", LLMApiType.OPENAI);

        // Local and self-hosted providers.
        this.register("ollama", "Ollama", "http://localhost:11434", LLMApiType.OLLAMA);
        this.register("lmstudio", "LM Studio", "http://localhost:1234/v1", LLMApiType.OPENAI);
        this.register("vllm", "vLLM", "http://localhost:8000/v1", LLMApiType.OPENAI);

        // Chinese model labs and cloud providers.
        this.register("deepseek-openai", "DeepSeek", "https://api.deepseek.com", LLMApiType.OPENAI);
        this.register("deepseek-anthropic", "DeepSeek", "https://api.deepseek.com/anthropic/v1", LLMApiType.ANTHROPIC);
        this.register("moonshot-cn-openai", "Moonshot Kimi (China)", "https://api.moonshot.cn/v1", LLMApiType.OPENAI);
        this.register("moonshot-cn-anthropic", "Moonshot Kimi (China)", "https://api.moonshot.cn/anthropic/v1", LLMApiType.ANTHROPIC);
        this.register("zhipu-cn-openai", "Zhipu GLM (China)", "https://open.bigmodel.cn/api/paas/v4", LLMApiType.OPENAI);
        this.register("zhipu-cn-anthropic", "Zhipu GLM (China)", "https://open.bigmodel.cn/api/anthropic/v1", LLMApiType.ANTHROPIC);
        this.register("qwen-cn-openai", "Alibaba Qwen (China)", "https://dashscope.aliyuncs.com/compatible-mode/v1", LLMApiType.OPENAI);
        this.register("doubao-openai", "Volcengine Doubao", "https://ark.cn-beijing.volces.com/api/v3", LLMApiType.OPENAI);
        this.register("hunyuan-openai", "Tencent Hunyuan", "https://api.hunyuan.cloud.tencent.com/v1", LLMApiType.OPENAI);
        this.register("tencent-tokenhub", "Tencent TokenHub", "https://tokenhub.tencentmaas.com/v1", LLMApiType.OPENAI);
        this.register("qianfan-openai", "Baidu Qianfan", "https://qianfan.baidubce.com/v2", LLMApiType.OPENAI);
        this.register("minimax-cn-openai", "MiniMax (China)", "https://api.minimax.cn/v1", LLMApiType.OPENAI);
        this.register("minimax-cn-anthropic", "MiniMax (China)", "https://api.minimax.cn/anthropic/v1", LLMApiType.ANTHROPIC);
        this.register("siliconflow-cn", "SiliconFlow (China)", "https://api.siliconflow.cn/v1", LLMApiType.OPENAI);
        this.register("stepfun-cn", "StepFun (China)", "https://api.stepfun.com/v1", LLMApiType.OPENAI);
        this.register("baichuan", "Baichuan AI", "https://api.baichuan-ai.com/v1", LLMApiType.OPENAI);
        this.register("spark", "iFlytek Spark", "https://spark-api-open.xf-yun.com/v1", LLMApiType.OPENAI);
        this.register("sensenova", "SenseTime SenseNova", "https://token.sensenova.cn/v1", LLMApiType.OPENAI);
        this.register("longcat", "Meituan LongCat", "https://api.longcat.chat/openai", LLMApiType.OPENAI);
        this.register("modelscope", "ModelScope", "https://api-inference.modelscope.cn/v1", LLMApiType.OPENAI);
        this.register("internlm", "Shanghai AI Lab InternLM", "https://internlm.intern-ai.org.cn/api/v1", LLMApiType.OPENAI);
        this.register("360-zhinao", "360 Zhinao", "https://api.360.cn/v1", LLMApiType.OPENAI);
        this.register("jiutian", "China Mobile Jiutian", "https://jiutian.10086.cn/largemodel/api/v1", LLMApiType.OPENAI);
        this.register("jdcloud", "JD Cloud", "https://modelservice.jdcloud.com/v1", LLMApiType.OPENAI);
        this.register("huawei-modelarts", "Huawei ModelArts", "https://api.modelarts-maas.com/v1", LLMApiType.OPENAI);
        this.register("tiangong", "Kunlun Tiangong", "https://api.tiangong.cn/v1", LLMApiType.OPENAI);
        this.register("drun", "D.Run", "https://chat.d.run/v1", LLMApiType.OPENAI);

        // Chinese inference platforms and aggregators.
        this.register("qiniu", "Qiniu AI", "https://api.qnaigc.com/v1", LLMApiType.OPENAI);
        this.register("ppio", "PPIO", "https://api.ppinfra.com/v3/openai", LLMApiType.OPENAI);
        this.register("jiekou", "Jiekou.AI", "https://api.jiekou.ai/openai", LLMApiType.OPENAI);
        this.register("aihubmix", "AIHubMix", "https://aihubmix.com/v1", LLMApiType.OPENAI);
        this.register("moark", "Moark", "https://moark.com/v1", LLMApiType.OPENAI);
    }
}
