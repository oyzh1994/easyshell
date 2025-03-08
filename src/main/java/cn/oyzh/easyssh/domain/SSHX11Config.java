package cn.oyzh.easyssh.domain;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.store.jdbc.Column;
import cn.oyzh.store.jdbc.PrimaryKey;
import cn.oyzh.store.jdbc.Table;
import lombok.Data;

import java.io.Serializable;

/**
 * @author oyzh
 * @since 2025/03/08
 */
@Data
@Table("t_x11_config")
public class SSHX11Config implements Serializable {

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
     * @see SSHConnect
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
}
