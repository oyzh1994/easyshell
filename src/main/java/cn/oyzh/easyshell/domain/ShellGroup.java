package cn.oyzh.easyshell.domain;


import cn.oyzh.fx.plus.domain.AppGroup;
import cn.oyzh.store.jdbc.Table;

/**
 * 分组
 *
 * @author oyzh
 * @since 2023/6/16
 */
@Table("t_group")
public class ShellGroup extends AppGroup {

    public ShellGroup() {
        super();
    }

    public ShellGroup(String gid, String name, boolean expand) {
        super(gid, name, expand);
    }
}
