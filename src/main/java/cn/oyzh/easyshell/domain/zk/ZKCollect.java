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
@Table("t_collect")
public class ZKCollect implements Serializable, ObjectCopier<ZKCollect> {

    /**
     * 连接id
     *
     * @see ZKConnect
     */
    @Column
    private String iid;

    /**
     * 路径
     */
    @Column
    private String path;

    public ZKCollect() {
    }

    public ZKCollect(String iid, String path) {
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
    public void copy(ZKCollect t1) {
        this.path = t1.getPath();
    }

    public static List<ZKCollect> clone(List<ZKCollect> collects) {
        if (CollectionUtil.isEmpty(collects)) {
            return Collections.emptyList();
        }
        List<ZKCollect> list = new ArrayList<>();
        for (ZKCollect collect : collects) {
            ZKCollect zkCollect = new ZKCollect();
            zkCollect.copy(collect);
            list.add(zkCollect);
        }
        return list;
    }
}
