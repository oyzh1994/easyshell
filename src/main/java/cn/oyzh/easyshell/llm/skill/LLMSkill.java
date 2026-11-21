package cn.oyzh.easyshell.llm.skill;

import cn.oyzh.common.util.BooleanUtil;
import cn.oyzh.common.util.NumberUtil;
import cn.oyzh.store.jdbc.Column;
import cn.oyzh.store.jdbc.PrimaryKey;
import cn.oyzh.store.jdbc.Table;

import java.io.Serializable;
import java.util.Objects;

/**
 * Agent Skill 配置。
 *
 * @author oyzh
 * @since 2026-11-21
 */
@Table("t_llm_skill")
public class LLMSkill implements Serializable {

    @Column
    @PrimaryKey
    private String uid;

    @Column
    private String name;

    /**
     * 托管 ZIP 文件的绝对路径。
     */
    @Column
    private String filePath;

    /**
     * 托管 ZIP 文件的 SHA-256。
     */
    @Column
    private String hash;

    /**
     * 上传或替换 ZIP 时记录的压缩文件大小。
     */
    @Column
    private Long fileSize;

    @Column
    private Boolean enabled = false;

    private transient LLMSkillValidationState validationState =
            LLMSkillValidationState.UNKNOWN;

    private transient String validationMessage;

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
        this.name = name == null ? "" : name.trim();
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public String getFileSizeText() {
        return this.fileSize == null || this.fileSize < 0
                ? ""
                : NumberUtil.formatSize(this.fileSize, 2);
    }

    public boolean isEnabled() {
        return BooleanUtil.isTrue(enabled);
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public LLMSkillValidationState getValidationState() {
        return this.validationState == null
                ? LLMSkillValidationState.UNKNOWN
                : this.validationState;
    }

    public void setValidationState(LLMSkillValidationState validationState,
                                   String validationMessage) {
        this.validationState = validationState == null
                ? LLMSkillValidationState.UNKNOWN
                : validationState;
        this.validationMessage = validationMessage;
    }

    public String getValidationMessage() {
        return validationMessage;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof LLMSkill that)) {
            return false;
        }
        return this.uid != null && Objects.equals(this.uid, that.uid);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.uid);
    }
}
