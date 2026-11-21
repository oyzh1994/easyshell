package cn.oyzh.easyshell.llm.prompt;

import cn.oyzh.store.jdbc.Column;
import cn.oyzh.store.jdbc.PrimaryKey;
import cn.oyzh.store.jdbc.Table;
import com.alibaba.fastjson2.annotation.JSONField;

import java.io.Serializable;
import java.util.Objects;

/**
 * 用户自定义提示词。
 *
 * @author oyzh
 * @since 2026-11-21
 */
@Table("t_llm_prompt")
public class LLMPrompt implements Serializable {

    @Column
    @PrimaryKey
    private String uid;

    @Column
    private String name;

    @Column
    private String content;

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
        this.name = LLMPromptUtil.normalizeName(name);
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = LLMPromptUtil.normalizeContent(content);
    }

    @JSONField(serialize = false, deserialize = false)
    public String getContentPreview() {
        return LLMPromptUtil.preview(this.content);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!(object instanceof LLMPrompt that)) {
            return false;
        }
        return this.uid != null && Objects.equals(this.uid, that.uid);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this.uid);
    }
}
