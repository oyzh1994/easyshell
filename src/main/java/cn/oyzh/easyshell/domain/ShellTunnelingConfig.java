package cn.oyzh.easyshell.domain;

import cn.oyzh.ssh.domain.SSHTunneling;
import cn.oyzh.store.jdbc.Column;
import cn.oyzh.store.jdbc.PrimaryKey;
import cn.oyzh.store.jdbc.Table;

import java.io.Serializable;

/**
 * @author oyzh
 * @since 2025-04-16
 */
@Table("t_tunneling_config")
public class ShellTunnelingConfig extends SSHTunneling implements Serializable {

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
