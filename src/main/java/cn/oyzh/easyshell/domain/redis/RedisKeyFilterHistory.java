package cn.oyzh.easyshell.domain.redis;

import cn.oyzh.common.object.ObjectComparator;
import cn.oyzh.store.jdbc.Column;
import cn.oyzh.store.jdbc.PrimaryKey;
import cn.oyzh.store.jdbc.Table;

import java.io.Serializable;
import java.util.Objects;

/**
 * redis键过滤历史
 *
 * @author oyzh
 * @since 2023/07/19
 */
@Table("t_key_filter_history")
public class RedisKeyFilterHistory implements ObjectComparator<RedisKeyFilterHistory>, Serializable {

    /**
     * 数据id
     */
    @Column
    @PrimaryKey
    private String uid;

    /**
     * 连接id
     * @see cn.oyzh.easyshell.domain.ShellConnect
     */
    @Column
    private String iid;

    /**
     * 模式
     */
    @Column
    private String pattern;

    /**
     * 保存时间
     */
    @Column
    private long saveTime = System.currentTimeMillis();

//    public RedisKeyFilterHistory() {
//    }
//
//    public RedisKeyFilterHistory(String uid, String pattern) {
//        this.uid = uid;
//        this.pattern = pattern;
//    }

    @Override
    public boolean compare(RedisKeyFilterHistory t1) {
        if (t1 == null) {
            return false;
        }
        if (Objects.equals(this, t1)) {
            return true;
        }
        return Objects.equals(this.pattern, t1.pattern);
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getPattern() {
        return pattern;
    }

    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    public long getSaveTime() {
        return saveTime;
    }

    public void setSaveTime(long saveTime) {
        this.saveTime = saveTime;
    }

    public String getIid() {
        return iid;
    }

    public void setIid(String iid) {
        this.iid = iid;
    }
}
