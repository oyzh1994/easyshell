package cn.oyzh.easyssh.domain;


import cn.oyzh.fx.plus.domain.TreeGroup;

/**
 * @author oyzh
 * @since 2023/6/16
 */
public class SSHGroup extends TreeGroup {

    public SSHGroup() {

    }

    public SSHGroup(String gid, String name, boolean expand) {
        super(gid, name, expand);
    }
}
