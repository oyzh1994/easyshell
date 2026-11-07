package cn.oyzh.easyshell.domain.valkey;

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
 * redis收藏
 *
 * @author oyzh
 * @since 2025-09-01
 */
@Table("t_valkey_collect")
public class ShellValkeyCollect implements Serializable, ObjectCopier<ShellValkeyCollect> {

    /**
     * 数据id
     */
    @Column
    @PrimaryKey
    private String uid;

    /**
     * 信息id
     *
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

    /**
     * 构造函数
     */
    public ShellValkeyCollect() {

    }

    /**
     * 构造函数
     *
     * @param iid     连接id
     * @param dbIndex db索引
     * @param key     键
     */
    public ShellValkeyCollect(String iid, int dbIndex, String key) {
        this.iid = iid;
        this.key = key;
        this.dbIndex = dbIndex;
    }

    /** 获取连接id */
    public String getIid() {
        return iid;
    }

    /** 设置连接id */
    public void setIid(String iid) {
        this.iid = iid;
    }

    /** 获取db索引 */
    public int getDbIndex() {
        return dbIndex;
    }

    /** 设置db索引 */
    public void setDbIndex(int dbIndex) {
        this.dbIndex = dbIndex;
    }

    /** 获取键 */
    public String getKey() {
        return key;
    }

    /** 设置键 */
    public void setKey(String key) {
        this.key = key;
    }

    /** 获取数据id */
    public String getUid() {
        return uid;
    }

    /** 设置数据id */
    public void setUid(String uid) {
        this.uid = uid;
    }

    @Override
    public void copy(ShellValkeyCollect t1) {
        this.key = t1.getKey();
        this.iid = t1.getIid();
        this.dbIndex = t1.getDbIndex();
    }

    /**
     * 克隆收藏列表
     *
     * @param collects 收藏列表
     * @return 克隆后的收藏列表
     */
    public static List<ShellValkeyCollect> clone(List<ShellValkeyCollect> collects) {
        if (CollectionUtil.isEmpty(collects)) {
            return Collections.emptyList();
        }
        List<ShellValkeyCollect> list = new ArrayList<>();
        for (ShellValkeyCollect collect : collects) {
            ShellValkeyCollect redisCollect = new ShellValkeyCollect();
            redisCollect.copy(collect);
            list.add(redisCollect);
        }
        return list;
    }

}
