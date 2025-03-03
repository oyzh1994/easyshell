package cn.oyzh.easyssh.domain;


import cn.oyzh.fx.plus.domain.AppGroup;

/**
 * @author oyzh
 * @since 2023/6/16
 */
public class SSHGroup extends AppGroup {

    public SSHGroup() {

    }

    public SSHGroup(String gid, String name, boolean expand) {
        super(gid, name, expand);
    }
}
