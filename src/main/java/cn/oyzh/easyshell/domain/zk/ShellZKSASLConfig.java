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
 * @since 2025-09-04
 */
@Table("t_zk_sasl_config")
public class ShellZKSASLConfig implements Serializable, ObjectCopier<ShellZKSASLConfig> {

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

    /** 获取数据id */
    public String getId() {
        return id;
    }

    /** 设置数据id */
    public void setId(String id) {
        this.id = id;
    }

    /** 获取zk连接id */
    public String getIid() {
        return iid;
    }

    /** 设置zk连接id */
    public void setIid(String iid) {
        this.iid = iid;
    }

    /** 获取sasl类型 */
    public String getType() {
        return type;
    }

    /** 设置sasl类型 */
    public void setType(String type) {
        this.type = type;
    }

    /** 获取用户名 */
    public String getUserName() {
        return userName;
    }

    /** 设置用户名 */
    public void setUserName(String userName) {
        this.userName = userName;
    }

    /** 获取密码 */
    public String getPassword() {
        return password;
    }

    /** 设置密码 */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * 检查是否无效
     *
     * @return 结果
     */
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
    public void copy(ShellZKSASLConfig t1) {
        this.type = t1.getType();
        this.userName = t1.getUserName();
        this.password = t1.getPassword();
    }

    /**
     * 克隆SASL配置
     *
     * @param config SASL配置
     * @return 克隆后的SASL配置
     */
    public static ShellZKSASLConfig clone(ShellZKSASLConfig config) {
        if (config == null) {
            return null;
        }
        ShellZKSASLConfig saslConfig = new ShellZKSASLConfig();
        saslConfig.copy(config);
        return saslConfig;
    }
}
