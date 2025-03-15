package cn.oyzh.easyshell.domain;


import cn.oyzh.fx.plus.domain.AppGroup;
import cn.oyzh.store.jdbc.Table;
import lombok.EqualsAndHashCode;

/**
 * @author oyzh
 * @since 2023/6/16
 */
@EqualsAndHashCode(callSuper = true)
@Table("t_group")
public class SSHGroup extends AppGroup {

    public SSHGroup() {

    }

    public SSHGroup(String gid, String name, boolean expand) {
        super(gid, name, expand);
    }
}
