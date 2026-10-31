package cn.oyzh.easyshell.llm;

import cn.oyzh.store.jdbc.Column;
import cn.oyzh.store.jdbc.PrimaryKey;
import cn.oyzh.store.jdbc.Table;
import com.alibaba.fastjson2.annotation.JSONField;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * LLM供应商
 *
 * @author oyzh
 * @since 2026-10-30
 */
@Table("t_llm_provider")
public class LLMProvider implements Serializable {

    /**
     * 数据id
     */
    @Column
    @PrimaryKey
    private String uid;

    /**
     * 名称
     */
    @Column
    private String name;

    /**
     * 基础地址
     */
    @Column
    private String baseUrl;

    /**
     * API密钥（SK）
     */
    @Column
    private String sk;

    /**
     * 默认模型
     */
    @Column
    private String defaultModel;

    /**
     * api类型
     * 0: openai
     * 1: anthropic
     * 2: gemini
     * 3: ollama
     */
    @Column
    private int apiType;

    /**
     * 是否为当前使用中的供应商
     */
    @Column
    private boolean active;

    /**
     * 模型列表，多个模型以换行符分隔
     */
    @Column
    private String modelsText;

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
     * 获取默认模型
     *
     * @return 默认模型
     */
    public String getDefaultModel() {
        if (this.defaultModel != null && !this.defaultModel.isBlank()) {
            return this.defaultModel;
        }
        List<String> models = this.getModels();
        return models.isEmpty() ? null : models.getFirst();
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
    @JSONField(serialize = false, deserialize = false)
    public List<String> getModels() {
        return parseModels(this.modelsText);
    }

    /**
     * 设置模型列表
     *
     * @param models 模型列表
     */
    @JSONField(serialize = false, deserialize = false)
    public void setModels(List<String> models) {
        this.modelsText = joinModels(models);
    }

    /**
     * 添加模型
     *
     * @param model 模型
     */
    public void addModel(String model) {
        if (model == null || model.isBlank()) {
            return;
        }
        List<String> models = new ArrayList<>(this.getModels());
        if (!models.contains(model)) {
            models.add(model);
            this.setModels(models);
        }
    }

    /**
     * 获取模型列表显示文本
     *
     * @return 模型列表显示文本
     */
    public String getModelsText() {
        StringBuilder builder = new StringBuilder();
        for (String model : this.getModels()) {
            if (!builder.isEmpty()) {
                builder.append((char) 44).append(' ');
            }
            builder.append(model);
        }
        return builder.toString();
    }

    /**
     * 设置模型列表文本
     *
     * @param modelsText 模型列表文本
     */
    public void setModelsText(String modelsText) {
        this.modelsText = modelsText;
    }

    /**
     * 获取api类型名称
     *
     * @return api类型名称
     */
    @JSONField(serialize = false, deserialize = false)
    public String getApiTypeName() {
        LLMApiType[] types = LLMApiType.values();
        if (this.apiType >= 0 && this.apiType < types.length) {
            return types[this.apiType].name();
        }
        return String.valueOf(this.apiType);
    }

    /**
     * 解析模型列表
     *
     * @param text 模型文本
     * @return 模型列表
     */
    public static List<String> parseModels(String text) {
        if (text == null || text.isBlank()) {
            return Collections.emptyList();
        }
        String normalized = text.replace((char) 44, '\n')
                .replace((char) 0xFF0C, '\n')
                .replace((char) 59, '\n')
                .replace((char) 0xFF1B, '\n');
        List<String> models = new ArrayList<>();
        for (String model : normalized.lines().toList()) {
            model = model.trim();
            if (!model.isEmpty() && !models.contains(model)) {
                models.add(model);
            }
        }
        return Collections.unmodifiableList(models);
    }

    private static String joinModels(List<String> models) {
        if (models == null || models.isEmpty()) {
            return null;
        }
        StringBuilder builder = new StringBuilder();
        for (String model : models) {
            if (model == null || model.isBlank()) {
                continue;
            }
            if (!builder.isEmpty()) {
                builder.append('\n');
            }
            builder.append(model.trim());
        }
        return builder.isEmpty() ? null : builder.toString();
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public int getApiType() {
        return apiType;
    }

    public void setApiType(int apiType) {
        this.apiType = apiType;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public String toString() {
        return this.name == null ? new String() : this.name;
    }
}
