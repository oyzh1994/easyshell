package cn.oyzh.easyshell.domain;

import cn.oyzh.store.jdbc.Column;
import cn.oyzh.store.jdbc.PrimaryKey;
import cn.oyzh.store.jdbc.Table;

import java.io.Serializable;

/**
 * shell终端历史
 *
 * @author oyzh
 * @since 2025/05/31
 */
@Table("t_term_history")
public class ShellTermHistory implements Serializable {

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

    public long getSaveTime() {
        return saveTime;
    }

    public void setSaveTime(long saveTime) {
        this.saveTime = saveTime;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIid() {
        return iid;
    }

    public void setIid(String iid) {
        this.iid = iid;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
