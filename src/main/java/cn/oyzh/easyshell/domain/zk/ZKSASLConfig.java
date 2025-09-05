package cn.oyzh.easyshell.domain.zk;

import cn.oyzh.common.object.ObjectCopier;
import cn.oyzh.store.jdbc.Column;
import cn.oyzh.store.jdbc.PrimaryKey;
import cn.oyzh.store.jdbc.Table;

import java.io.Serializable;

/**
 * zk连接sasl配置
 *
 * @author oyzh
 * @since 2024-12-20
 */
@Table("t_zk_sasl_config")
public class ZKSASLConfig implements Serializable, ObjectCopier<ZKSASLConfig> {

    /**
     * 数据id
     */
    @Column
    @PrimaryKey
    private String id;
    /**
     * zk连接id
     *
     * @see cn.oyzh.easyshell.domain.ShellConnect
     */
    @Column
    private String iid;

    /**
     * sasl类型
     */
    @Column
    private String type;

    /**
     * 用户名
     */
    @Column
    private String userName;

    /**
     * 密码
     */
    @Column
    private String password;

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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean checkInvalid() {
        if (this.iid == null) {
            return true;
        }
        if ("Digest".equalsIgnoreCase(this.type)) {
            return this.userName == null || this.password == null;
        }
        return false;
    }

    @Override
    public void copy(ZKSASLConfig t1) {
        this.type = t1.getType();
        this.userName = t1.getUserName();
        this.password = t1.getPassword();
    }

    public static ZKSASLConfig clone(ZKSASLConfig config) {
        if (config == null) {
            return null;
        }
        ZKSASLConfig saslConfig = new ZKSASLConfig();
        saslConfig.copy(config);
        return saslConfig;
    }
}
