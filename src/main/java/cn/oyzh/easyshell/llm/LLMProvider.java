package cn.oyzh.easyshell.llm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * LLM供应商
 *
 * @author oyzh
 * @since 2026-10-30
 */
public class LLMProvider {

    /**
     * 名称
     */
    private String name;

    /**
     * 基础地址
     */
    private String baseUrl;

    /**
     * API密钥（SK）
     */
    private String sk;

    /**
     * 默认模型
     */
    private String defaultModel;

    /**
     * 模型列表
     */
    private final List<String> models = new ArrayList<>();

    public LLMProvider() {
    }

    public LLMProvider(String name, String baseUrl, String sk, List<String> models) {
        this.name = name;
        this.baseUrl = baseUrl;
        this.sk = sk;
        this.setModels(models);
    }

    /**
     * 获取名称
     *
     * @return 名称
     */
    public String getName() {
        return name;
    }

    /**
     * 设置名称
     *
     * @param name 名称
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 获取基础地址
     *
     * @return 基础地址
     */
    public String getBaseUrl() {
        return baseUrl;
    }

    /**
     * 设置基础地址
     *
     * @param baseUrl 基础地址
     */
    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    /**
     * 获取API密钥（SK）
     *
     * @return API密钥
     */
    public String getSk() {
        return sk;
    }

    /**
     * 设置API密钥（SK）
     *
     * @param sk API密钥
     */
    public void setSk(String sk) {
        this.sk = sk;
    }

    /**
     * 获取API密钥
     *
     * @return API密钥
     */
    public String getApiKey() {
        return sk;
    }

    /**
     * 设置API密钥
     *
     * @param apiKey API密钥
     */
    public void setApiKey(String apiKey) {
        this.sk = apiKey;
    }

    /**
     * 获取默认模型
     *
     * @return 默认模型
     */
    public String getDefaultModel() {
        if (this.defaultModel != null && !this.defaultModel.isBlank()) {
            return this.defaultModel;
        }
        return this.models.isEmpty() ? null : this.models.getFirst();
    }

    /**
     * 设置默认模型
     *
     * @param defaultModel 默认模型
     */
    public void setDefaultModel(String defaultModel) {
        this.defaultModel = defaultModel;
    }

    /**
     * 获取模型列表
     *
     * @return 模型列表
     */
    public List<String> getModels() {
        return Collections.unmodifiableList(this.models);
    }

    /**
     * 设置模型列表
     *
     * @param models 模型列表
     */
    public void setModels(List<String> models) {
        this.models.clear();
        if (models != null) {
            this.models.addAll(models);
        }
    }

    /**
     * 添加模型
     *
     * @param model 模型
     */
    public void addModel(String model) {
        if (model != null && !model.isBlank() && !this.models.contains(model)) {
            this.models.add(model);
        }
    }

    @Override
    public String toString() {
        return this.name == null ? "" : this.name;
    }
}
