package cn.oyzh.easyshell.llm.capability;

import cn.oyzh.easyshell.llm.LLMFileContentType;
import cn.oyzh.easyshell.llm.LLMModelContextWindowUtil;
import cn.oyzh.store.jdbc.Column;
import cn.oyzh.store.jdbc.PrimaryKey;
import cn.oyzh.store.jdbc.Table;

import java.io.Serializable;

/**
 * Capability metadata for one provider model.
 *
 * @author oyzh
 * @since 2026-11-21
 */
@Table("t_llm_model_capability")
public class LLMModelCapability implements Serializable {

    @Column
    @PrimaryKey
    private String uid;

    @Column
    private String providerUid;

    @Column
    private String model;

    @Column
    private int contextWindowTokens = LLMModelContextWindowUtil.DEFAULT_TOKENS;

    @Column
    private boolean tools = true;

    @Column
    private boolean image;

    @Column
    private boolean pdf;

    @Column
    private boolean audio;

    @Column
    private boolean video;

    @Column
    private boolean thinking = true;

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getProviderUid() {
        return providerUid;
    }

    public void setProviderUid(String providerUid) {
        this.providerUid = providerUid;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public int getContextWindowTokens() {
        return LLMModelContextWindowUtil.normalize(this.contextWindowTokens);
    }

    public void setContextWindowTokens(int contextWindowTokens) {
        this.contextWindowTokens = LLMModelContextWindowUtil.normalize(contextWindowTokens);
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

    /**
     * Checks whether the model supports an attachment category.
     *
     * @param type attachment category
     * @return whether supported
     */
    public boolean supports(LLMFileContentType type) {
        return switch (type) {
            case TEXT -> true;
            case IMAGE -> this.image;
            case PDF -> this.pdf;
            case AUDIO -> this.audio;
            case VIDEO -> this.video;
        };
    }

    /**
     * Creates a detached mutable copy.
     *
     * @return copy
     */
    public LLMModelCapability copy() {
        LLMModelCapability result = new LLMModelCapability();
        result.setUid(this.uid);
        result.setProviderUid(this.providerUid);
        result.setModel(this.model);
        result.setContextWindowTokens(this.contextWindowTokens);
        result.setTools(this.tools);
        result.setImage(this.image);
        result.setPdf(this.pdf);
        result.setAudio(this.audio);
        result.setVideo(this.video);
        result.setThinking(this.thinking);
        return result;
    }
}
