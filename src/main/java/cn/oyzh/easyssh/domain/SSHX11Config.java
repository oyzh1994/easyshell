package cn.oyzh.easyssh.domain;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.store.jdbc.Column;
import cn.oyzh.store.jdbc.PrimaryKey;
import cn.oyzh.store.jdbc.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.io.Serializable;

@Data
@Table("t_x11_config")
public class SSHX11Config implements Serializable {

    @Column
    private int port;

    @PrimaryKey
    private String id;

    @Column
    private String iid;

    @Column
    private String host;

    public boolean isLocal() {
        return StringUtil.equalsAnyIgnoreCase(this.host, "localhost", "127.0.0.1");
    }
}
