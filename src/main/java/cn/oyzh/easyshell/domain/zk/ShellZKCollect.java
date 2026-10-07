package cn.oyzh.easyshell.domain.zk;

import cn.oyzh.common.object.ObjectCopier;
import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.store.jdbc.Column;
import cn.oyzh.store.jdbc.Table;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * zk收藏
 *
 * @author oyzh
 * @since 2025-09-04
 */
@Table("t_zk_collect")
public class ShellZKCollect implements Serializable, ObjectCopier<ShellZKCollect> {

    /**
     * 连接id
     *
     * @see cn.oyzh.easyshell.domain.ShellConnect
     */
    @Column
    private String iid;

    /**
     * 路径
     */
    @Column
    private String path;

    /**
     * 构造函数
     */
    public ShellZKCollect() {
    }

    /**
     * 构造函数
     *
     * @param iid  连接id
     * @param path 路径
     */
    public ShellZKCollect(String iid, String path) {
        this.iid = iid;
        this.path = path;
    }

    /** 获取连接id */
    public String getIid() {
        return iid;
    }

    /** 设置连接id */
    public void setIid(String iid) {
        this.iid = iid;
    }

    /** 获取路径 */
    public String getPath() {
        return path;
    }

    /** 设置路径 */
    public void setPath(String path) {
        this.path = path;
    }

    @Override
    public void copy(ShellZKCollect t1) {
        this.path = t1.getPath();
    }

    /**
     * 克隆收藏列表
     *
     * @param collects 收藏列表
     * @return 克隆后的收藏列表
     */
    public static List<ShellZKCollect> clone(List<ShellZKCollect> collects) {
        if (CollectionUtil.isEmpty(collects)) {
            return Collections.emptyList();
        }
        List<ShellZKCollect> list = new ArrayList<>();
        for (ShellZKCollect collect : collects) {
            ShellZKCollect zkCollect = new ShellZKCollect();
            zkCollect.copy(collect);
            list.add(zkCollect);
        }
        return list;
    }
}
