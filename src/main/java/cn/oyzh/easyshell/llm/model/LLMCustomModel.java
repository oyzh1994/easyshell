package cn.oyzh.easyshell.llm.model;

import cn.oyzh.easyshell.llm.LLMModelContextWindowUtil;
import cn.oyzh.easyshell.llm.capability.LLMModelCapability;
import cn.oyzh.i18n.I18nResourceBundle;
import cn.oyzh.store.jdbc.Column;
import cn.oyzh.store.jdbc.PrimaryKey;
import cn.oyzh.store.jdbc.Table;

import java.io.Serializable;

/**
 * 用户自定义 LLM 模型。
 *
 * @author oyzh
 * @since 2026-11-24
 */
@Table("t_llm_custom_model")
public class LLMCustomModel implements Serializable {

    @Column
    @PrimaryKey
    private String uid;

    /**
     * 显示名称
     */
    @Column
    private String name;

    /**
     * 请求时使用的模型 id
     */
    @Column
    private String modelId;

    /**
     * 上下文窗口
     */
    @Column
    private int contextWindowTokens = LLMModelContextWindowUtil.DEFAULT_TOKENS;

    @Column
    private boolean tools = true;

    @Column
    private boolean image = true;

    @Column
    private boolean pdf;

    @Column
    private boolean audio;

    @Column
    private boolean video;

    @Column
    private boolean thinking = true;

    /**
     * 是否启用
     */
    @Column
    private boolean enabled = true;

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getModelId() {
        return modelId;
    }

    public void setModelId(String modelId) {
        this.modelId = modelId;
    }

    public int getContextWindowTokens() {
        return LLMModelContextWindowUtil.normalize(this.contextWindowTokens);
    }

    public void setContextWindowTokens(int contextWindowTokens) {
        this.contextWindowTokens = LLMModelContextWindowUtil.normalize(contextWindowTokens);
    }

    public String getContextWindowText() {
        return LLMModelContextWindowUtil.label(this.getContextWindowTokens());
    }

    public boolean isTools() {
        return tools;
    }

    public void setTools(boolean tools) {
        this.tools = tools;
    }

    public boolean isImage() {
        return image;
    }

    public void setImage(boolean image) {
        this.image = image;
    }

    public boolean isPdf() {
        return pdf;
    }

    public void setPdf(boolean pdf) {
        this.pdf = pdf;
    }

    public boolean isAudio() {
        return audio;
    }

    public void setAudio(boolean audio) {
        this.audio = audio;
    }

    public boolean isVideo() {
        return video;
    }

    public void setVideo(boolean video) {
        this.video = video;
    }

    public boolean isThinking() {
        return thinking;
    }

    public void setThinking(boolean thinking) {
        this.thinking = thinking;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * 模型下拉列表显示名称
     *
     * @return 显示名称
     */
    public String getDisplayName() {
        if (this.name != null && !this.name.isBlank()) {
            return this.name.trim();
        }
        return this.modelId == null ? "" : this.modelId.trim();
    }

    /**
     * 能力摘要文本
     *
     * @return 能力摘要
     */
    public String getCapabilitiesText() {
        StringBuilder result = new StringBuilder();
        this.appendCapability(result, this.tools, "shell.llm.modelCapability.tools");
        this.appendCapability(result, this.image, "shell.llm.modelCapability.image");
        this.appendCapability(result, this.pdf, "shell.llm.modelCapability.pdf");
        this.appendCapability(result, this.audio, "shell.llm.modelCapability.audio");
        this.appendCapability(result, this.video, "shell.llm.modelCapability.video");
        this.appendCapability(result, this.thinking, "shell.llm.modelCapability.thinking");
        return result.toString();
    }

    private void appendCapability(StringBuilder result, boolean enabled, String key) {
        if (!enabled) {
            return;
        }
        if (!result.isEmpty()) {
            result.append(", ");
        }
        result.append(I18nResourceBundle.i18nString(key));
    }

    /**
     * 转换为能力配置
     *
     * @return 能力配置
     */
    public LLMModelCapability toCapability() {
        LLMModelCapability capability = new LLMModelCapability();
        capability.setModel(this.modelId);
        capability.setContextWindowTokens(this.getContextWindowTokens());
        capability.setTools(this.tools);
        capability.setImage(this.image);
        capability.setPdf(this.pdf);
        capability.setAudio(this.audio);
        capability.setVideo(this.video);
        capability.setThinking(this.thinking);
        return capability;
    }

    /**
     * 复制当前模型，编辑时避免取消操作污染原对象。
     *
     * @return 副本
     */
    public LLMCustomModel copy() {
        LLMCustomModel result = new LLMCustomModel();
        result.setUid(this.uid);
        result.setName(this.name);
        result.setModelId(this.modelId);
        result.setContextWindowTokens(this.contextWindowTokens);
        result.setTools(this.tools);
        result.setImage(this.image);
        result.setPdf(this.pdf);
        result.setAudio(this.audio);
        result.setVideo(this.video);
        result.setThinking(this.thinking);
        result.setEnabled(this.enabled);
        return result;
    }

    @Override
    public String toString() {
        return this.getDisplayName();
    }
}
