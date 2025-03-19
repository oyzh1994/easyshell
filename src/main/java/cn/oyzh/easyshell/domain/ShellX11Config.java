package cn.oyzh.easyshell.domain;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.store.jdbc.Column;
import cn.oyzh.store.jdbc.PrimaryKey;
import cn.oyzh.store.jdbc.Table;

import java.io.Serializable;

/**
 * @author oyzh
 * @since 2025/03/08
 */
@Table("t_x11_config")
public class ShellX11Config implements Serializable {

    /**
     * 端口
     */
    @Column
    private int port;

    /**
     * 数据id
     */
    @PrimaryKey
    private String id;

    /**
     * 连接id
     *
     * @see ShellConnect
     */
    @Column
    private String iid;

    /**
     * 地址
     */
    @Column
    private String host;

    /**
     * 是否本地转发
     *
     * @return 结果
     */
    public boolean isLocal() {
        return StringUtil.equalsAnyIgnoreCase(this.host, "localhost", "127.0.0.1");
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getIid() {
        return iid;
    }

    public void setIid(String iid) {
        this.iid = iid;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }
}
