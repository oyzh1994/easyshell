package cn.oyzh.easyshell.domain;

import cn.oyzh.store.jdbc.Column;
import cn.oyzh.store.jdbc.PrimaryKey;
import cn.oyzh.store.jdbc.Table;

import java.io.Serializable;

/**
 * shell片段
 *
 * @author oyzh
 * @since 2025-06-01
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

    /** 获取数据id */
    public String getId() {
        return id;
    }

    /** 设置数据id */
    public void setId(String id) {
        this.id = id;
    }

    /** 获取名称 */
    public String getName() {
        return name;
    }

    /** 设置名称 */
    public void setName(String name) {
        this.name = name;
    }

    /** 获取内容 */
    public String getContent() {
        return content;
    }

    /** 设置内容 */
    public void setContent(String content) {
        this.content = content;
    }
}
