package cn.oyzh.easyshell.domain;

import cn.oyzh.store.jdbc.Column;
import cn.oyzh.store.jdbc.PrimaryKey;
import cn.oyzh.store.jdbc.Table;

import java.io.Serializable;

/**
 * shell文件收藏
 *
 * @author oyzh
 * @since 2025/06/03
 */
@Table("t_file_collect")
public class ShellFileCollect implements Serializable {

    /**
     * 数据id
     */
    @Column
    @PrimaryKey
    private String id;

    /**
     * 所属连接id
     */
    @Column
    private String iid;

    /**
     * 保存时间
     */
    @Column
    private long saveTime;

    /**
     * 内容
     */
    @Column
    private String content;

    /** 获取保存时间 */
    public long getSaveTime() {
        return saveTime;
    }

    /** 设置保存时间 */
    public void setSaveTime(long saveTime) {
        this.saveTime = saveTime;
    }

    /** 获取数据id */
    public String getId() {
        return id;
    }

    /** 设置数据id */
    public void setId(String id) {
        this.id = id;
    }

    /** 获取所属连接id */
    public String getIid() {
        return iid;
    }

    /** 设置所属连接id */
    public void setIid(String iid) {
        this.iid = iid;
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
