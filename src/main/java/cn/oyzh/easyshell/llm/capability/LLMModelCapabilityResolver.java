package cn.oyzh.easyshell.llm.capability;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.llm.LLMApiType;
import cn.oyzh.easyshell.llm.LLMFileContentType;
import cn.oyzh.easyshell.llm.LLMModelContextWindowUtil;
import cn.oyzh.easyshell.llm.provider.LLMProvider;

import java.util.Locale;

/**
 * Resolves built-in model capabilities with persisted user overrides.
 *
 * @author oyzh
 * @since 2026-11-21
 */
public final class LLMModelCapabilityResolver {

    private LLMModelCapabilityResolver() {
    }

    /**
     * Resolves capabilities for a provider model.
     *
     * @param provider provider
     * @param model    model id
     * @return capabilities
     */
    public static LLMModelCapability resolve(LLMProvider provider, String model) {
        String providerUid = provider == null ? null : provider.getUid();
        String modelName = StringUtil.isBlank(model)
                ? provider == null ? null : provider.getDefaultModel()
                : model.trim();
        if (StringUtil.isNotBlank(providerUid) && StringUtil.isNotBlank(modelName)) {
            LLMModelCapability override = LLMModelCapabilityStore.INSTANCE.find(
                    providerUid, modelName);
            if (override != null) {
                override.setModel(modelName);
                override.setProviderUid(providerUid);
                return override;
            }
        }
        return builtIn(provider, modelName);
    }

    /**
     * Saves a capability override.
     *
     * @param provider   provider
     * @param model      model id
     * @param capability capability
     * @return whether saved
     */
    public static boolean save(LLMProvider provider,
                               String model,
                               LLMModelCapability capability) {
        if (provider == null || StringUtil.isBlank(provider.getUid())
                || StringUtil.isBlank(model) || capability == null) {
            return false;
        }
        capability.setProviderUid(provider.getUid());
        capability.setModel(model.trim());
        capability.setContextWindowTokens(capability.getContextWindowTokens());
        return LLMModelCapabilityStore.INSTANCE.save(capability);
    }

    static LLMModelCapability builtIn(LLMProvider provider, String model) {
        LLMModelCapability result = new LLMModelCapability();
        result.setProviderUid(provider == null ? null : provider.getUid());
        result.setModel(model);
        result.setContextWindowTokens(LLMModelContextWindowUtil.DEFAULT_TOKENS);
        if (provider == null || StringUtil.isBlank(model)) {
            result.setTools(false);
            return result;
        }
        String name = model.toLowerCase(Locale.ROOT);
        LLMApiType apiType = LLMApiType.fromOrdinal(provider.getApiType());
        boolean clearlyChatModel = !(name.contains("embedding")
                || name.contains("embed")
                || name.contains("rerank")
                || name.contains("whisper")
                || name.contains("moderation")
                || name.contains("tts")
                || name.contains("dall-e")
                || name.contains("image-1"));
        result.setTools(clearlyChatModel);
        result.setThinking(clearlyChatModel);
        // 当前主流聊天模型默认具备图片理解能力，特殊模型仍可通过能力覆盖关闭
        result.setImage(clearlyChatModel);
        result.setPdf(apiType == LLMApiType.GEMINI
                || apiType == LLMApiType.ANTHROPIC && name.contains("claude"));
        result.setAudio(apiType == LLMApiType.GEMINI
                || name.contains("audio")
                || name.contains("realtime"));
        result.setVideo(apiType == LLMApiType.GEMINI
                || name.contains("video")
                || name.contains("realtime"));
        return result;
    }

    /**
     * Returns a concise user-facing capability summary.
     *
     * @param capability capability
     * @return summary
     */
    public static String attachmentSummary(LLMModelCapability capability) {
        StringBuilder result = new StringBuilder("TEXT");
        if (capability.isImage()) {
            result.append(", IMAGE");
        }
        if (capability.isPdf()) {
            result.append(", PDF");
        }
        if (capability.isAudio()) {
            result.append(", AUDIO");
        }
        if (capability.isVideo()) {
            result.append(", VIDEO");
        }
        return result.toString();
    }
}
