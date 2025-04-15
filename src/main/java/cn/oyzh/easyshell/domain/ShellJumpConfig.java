package cn.oyzh.easyshell.domain;

import cn.oyzh.ssh.domain.SSHConnect;
import cn.oyzh.store.jdbc.Column;
import cn.oyzh.store.jdbc.PrimaryKey;
import cn.oyzh.store.jdbc.Table;

import java.io.Serializable;

/**
 * shell跳板配置
 *
 * @author oyzh
 * @since 2025-03-15
 */
@Table("t_jump_config")
public class ShellJumpConfig extends SSHConnect implements Serializable {

    /**
     * id
     *
     */
    @Column
    @PrimaryKey
    private String id;

    /**
     * 连接id
     *
     * @see ShellConnect
     */
    @Column
    private String iid;

    public String getIid() {
        return iid;
    }

    public void setIid(String iid) {
        this.iid = iid;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
