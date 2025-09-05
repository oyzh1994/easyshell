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
 * @since 2024-09-26
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

    public ShellZKCollect() {
    }

    public ShellZKCollect(String iid, String path) {
        this.iid = iid;
        this.path = path;
    }

    public String getIid() {
        return iid;
    }

    public void setIid(String iid) {
        this.iid = iid;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @Override
    public void copy(ShellZKCollect t1) {
        this.path = t1.getPath();
    }

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
