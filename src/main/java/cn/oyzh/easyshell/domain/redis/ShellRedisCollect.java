package cn.oyzh.easyshell.domain.redis;

import cn.oyzh.common.object.ObjectCopier;
import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.store.jdbc.Column;
import cn.oyzh.store.jdbc.PrimaryKey;
import cn.oyzh.store.jdbc.Table;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author oyzh
 * @since 2024-09-26
 */
@Table("t_redis_collect")
public class ShellRedisCollect implements Serializable, ObjectCopier<ShellRedisCollect> {

    /**
     * 数据id
     */
    @Column
    @PrimaryKey
    private String uid;

    /**
     * 信息id
     * @see cn.oyzh.easyshell.domain.ShellConnect
     */
    @Column
    private String iid;

    /**
     * db索引
     */
    @Column
    private int dbIndex;

    /**
     * 键
     */
    @Column
    private String key;

    public ShellRedisCollect() {

    }

    public ShellRedisCollect(String iid, int dbIndex, String key) {
        this.iid = iid;
        this.key = key;
        this.dbIndex = dbIndex;
    }

    public String getIid() {
        return iid;
    }

    public void setIid(String iid) {
        this.iid = iid;
    }

    public int getDbIndex() {
        return dbIndex;
    }

    public void setDbIndex(int dbIndex) {
        this.dbIndex = dbIndex;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    @Override
    public void copy(ShellRedisCollect t1) {
        this.key = t1.getKey();
        this.iid = t1.getIid();
        this.dbIndex = t1.getDbIndex();
    }

    public static List<ShellRedisCollect> clone(List<ShellRedisCollect> collects) {
        if (CollectionUtil.isEmpty(collects)) {
            return Collections.emptyList();
        }
        List<ShellRedisCollect> list = new ArrayList<>();
        for (ShellRedisCollect collect : collects) {
            ShellRedisCollect redisCollect = new ShellRedisCollect();
            redisCollect.copy(collect);
            list.add(redisCollect);
        }
        return list;
    }

}
