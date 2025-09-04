package cn.oyzh.easyshell.domain.zk;

import cn.oyzh.common.object.ObjectComparator;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.fx.plus.domain.AppGroup;
import cn.oyzh.store.jdbc.Table;

/**
 * zk连接分组
 *
 * @author oyzh
 * @since 2023/5/12
 */
@Table("t_group")
public class ZKGroup extends AppGroup implements ObjectComparator<ZKGroup> {

    public ZKGroup() {
        super();
    }

    public ZKGroup(String gid, String name, boolean expand) {
        super(gid, name, expand);
    }

    @Override
    public boolean compare(ZKGroup t1) {
        if (t1 == null) {
            return false;
        }
        return StringUtil.equals(this.getName(), t1.getName());
    }
}

