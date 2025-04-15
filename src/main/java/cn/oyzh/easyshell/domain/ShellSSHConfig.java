package cn.oyzh.easyshell.domain;

import cn.oyzh.ssh.domain.SSHConnect;
import cn.oyzh.store.jdbc.Column;
import cn.oyzh.store.jdbc.PrimaryKey;
import cn.oyzh.store.jdbc.Table;

import java.io.Serializable;

/**
 * shell连接ssh配置
 *
 * @author oyzh
 * @since 2025-03-15
 */
@Table("t_ssh_config")
public class ShellSSHConfig extends SSHConnect implements Serializable {

    /**
     * 连接id
     *
     * @see ShellConnect
     */
    @Column
    @PrimaryKey
    private String iid;

    public String getIid() {
        return iid;
    }

    public void setIid(String iid) {
        this.iid = iid;
    }
}
