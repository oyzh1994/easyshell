package cn.oyzh.easyshell.domain;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.store.jdbc.Column;
import cn.oyzh.store.jdbc.PrimaryKey;
import cn.oyzh.store.jdbc.Table;

import java.io.Serializable;

/**
 * shell查询
 *
 * @author oyzh
 * @since 2025-01-20
 */
@Table("t_query")
public class ShellQuery implements Serializable {

    /**
     * 连接id
     *
     * @see ShellConnect
     */
    @Column
    private String iid;

    /**
     * 主键
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
     * 内容
     */
    @Column
    private String content;

    /**
     * db索引
     */
    @Column
    private int dbIndex;

    /**
     * 数据库名称
     */
    @Column
    private String dbName;

    /** 获取连接id */
    public String getIid() {
        return iid;
    }

    /** 设置连接id */
    public void setIid(String iid) {
        this.iid = iid;
    }

    /** 获取主键 */
    public String getUid() {
        return uid;
    }

    /** 设置主键 */
    public void setUid(String uid) {
        this.uid = uid;
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

    /** 获取db索引 */
    public int getDbIndex() {
        return dbIndex;
    }

    /** 设置db索引 */
    public void setDbIndex(int dbIndex) {
        this.dbIndex = dbIndex;
    }

    /** 获取数据库名称 */
    public String getDbName() {
        return dbName;
    }

    /** 设置数据库名称 */
    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    /**
     * 是否新查询
     *
     * @return 结果
     */
    public boolean isNew() {
        return StringUtil.isBlank(this.getUid());
    }
}
