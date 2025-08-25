package cn.oyzh.easyshell.domain;

import cn.oyzh.store.jdbc.Column;
import cn.oyzh.store.jdbc.PrimaryKey;
import cn.oyzh.store.jdbc.Table;

import java.io.Serializable;

/**
 * 片段
 *
 * @author oyzh
 * @since 2025-06-11
 */
@Table("t_snippet")
public class ShellSnippet implements Serializable {

    /**
     * 数据id
     */
    @Column
    @PrimaryKey
    private String id;

    /**
     * 名称
     */
    @Column
    private String name;

    /**
     * 内容
     */
    @Column
    private String content;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
