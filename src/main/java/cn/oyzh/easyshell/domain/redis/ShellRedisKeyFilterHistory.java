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
@Table("t_redis_key_filter_history")
public class ShellRedisKeyFilterHistory implements ObjectComparator<ShellRedisKeyFilterHistory>, Serializable {

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

//    public ShellRedisKeyFilterHistory() {
//    }
//
//    public ShellRedisKeyFilterHistory(String uid, String pattern) {
//        this.uid = uid;
//        this.pattern = pattern;
//    }

    @Override
    public boolean compare(ShellRedisKeyFilterHistory t1) {
        if (t1 == null) {
            return false;
        }
        if (Objects.equals(this, t1)) {
            return true;
        }
        return Objects.equals(this.pattern, t1.pattern);
    }

    /** 获取数据id */
    public String getUid() {
        return uid;
    }

    /** 设置数据id */
    public void setUid(String uid) {
        this.uid = uid;
    }

    /** 获取模式 */
    public String getPattern() {
        return pattern;
    }

    /** 设置模式 */
    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    /** 获取保存时间 */
    public long getSaveTime() {
        return saveTime;
    }

    /** 设置保存时间 */
    public void setSaveTime(long saveTime) {
        this.saveTime = saveTime;
    }

    /** 获取连接id */
    public String getIid() {
        return iid;
    }

    /** 设置连接id */
    public void setIid(String iid) {
        this.iid = iid;
    }
}
