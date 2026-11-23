package cn.oyzh.easyshell.llm.model;

import cn.oyzh.easyshell.llm.capability.LLMModelCapability;
import cn.oyzh.easyshell.llm.capability.LLMModelCapabilityResolver;
import cn.oyzh.easyshell.llm.provider.LLMProvider;

/**
 * 模型下拉列表中的选项。
 *
 * @author oyzh
 * @since 2026-11-24
 */
public final class LLMModelChoice {

    /**
     * 下拉显示名称
     */
    private final String name;

    /**
     * 实际请求模型 id
     */
    private final String modelId;

    /**
     * 持久化标识。供应商模型沿用模型 id，自定义模型使用 custom:uid。
     */
    private final String key;

    /**
     * 自定义模型，供应商模型为 null
     */
    private final LLMCustomModel customModel;

    private LLMModelChoice(String name, String modelId, String key, LLMCustomModel customModel) {
        this.name = name;
        this.modelId = modelId;
        this.key = key;
        this.customModel = customModel;
    }

    public static LLMModelChoice provider(String model) {
        String value = model == null ? "" : model.trim();
        return new LLMModelChoice(value, value, value, null);
    }

    public static LLMModelChoice custom(LLMCustomModel model) {
        return new LLMModelChoice(model.getDisplayName(), model.getModelId(),
                "custom:" + model.getUid(), model);
    }

    public String getName() {
        return name;
    }

    public String getModelId() {
        return modelId;
    }

    public String getKey() {
        return key;
    }

    public LLMCustomModel getCustomModel() {
        return customModel;
    }

    public boolean isCustom() {
        return this.customModel != null;
    }

    /**
     * 解析当前模型选项的能力配置。
     *
     * @param provider 当前供应商
     * @return 能力配置
     */
    public LLMModelCapability capability(LLMProvider provider) {
        if (this.customModel != null) {
            return this.customModel.toCapability();
        }
        return LLMModelCapabilityResolver.resolve(provider, this.modelId);
    }

    @Override
    public String toString() {
        return this.name;
    }
}
