package cn.oyzh.easyshell.domain;

import cn.oyzh.common.object.ObjectCopier;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.store.jdbc.Column;
import cn.oyzh.store.jdbc.PrimaryKey;
import cn.oyzh.store.jdbc.Table;
import com.alibaba.fastjson2.annotation.JSONField;

import java.io.Serializable;

/**
 * shell ssl配置
 *
 * @author oyzh
 * @since 2025-09-04
 */
@Table("t_ssl")
public class ShellSSLConfig implements Serializable, ObjectCopier<ShellSSLConfig> {

    /**
     * 数据id
     */
    @Column
    @PrimaryKey
    private String id;

    /**
     * 所属连接id
     */
    @Column
    private String iid;

    /**
     * 客户端密钥
     */
    @Column
    private String clientKey;

    /**
     * 客户端密码
     */
    @Column
    private String clientPwd;

    /**
     * 客户端证书
     */
    @Column
    private String clientCrt;

    /**
     * ca证书
     */
    @Column
    private String caCrt;

    /** 获取所属连接id */
    public String getIid() {
        return iid;
    }

    /** 设置所属连接id */
    public void setIid(String iid) {
        this.iid = iid;
    }

    /** 获取数据id */
    public String getId() {
        return id;
    }

    /** 设置数据id */
    public void setId(String id) {
        this.id = id;
    }

    /** 获取客户端密钥 */
    public String getClientKey() {
        return clientKey;
    }

    /** 设置客户端密钥 */
    public void setClientKey(String clientKey) {
        this.clientKey = clientKey;
    }

    /** 获取客户端证书 */
    public String getClientCrt() {
        return clientCrt;
    }

    /** 设置客户端证书 */
    public void setClientCrt(String clientCrt) {
        this.clientCrt = clientCrt;
    }

    /** 获取ca证书 */
    public String getCaCrt() {
        return caCrt;
    }

    /** 设置ca证书 */
    public void setCaCrt(String caCrt) {
        this.caCrt = caCrt;
    }

    /** 获取客户端密码 */
    public String getClientPwd() {
        return clientPwd;
    }

    /** 设置客户端密码 */
    public void setClientPwd(String clientPwd) {
        this.clientPwd = clientPwd;
    }

    @Override
    public void copy(ShellSSLConfig t1) {
        this.caCrt = t1.caCrt;
        this.clientCrt = t1.clientCrt;
        this.clientKey = t1.clientKey;
        this.clientPwd = t1.clientPwd;
    }

    /**
     * 是否无效
     *
     * @return 结果
     */
    @JSONField(serialize = false, deserialize = false)
    public boolean isInvalid() {
        return StringUtil.isBlank(this.caCrt)
                || StringUtil.isBlank(this.clientCrt)
                || StringUtil.isBlank(this.clientKey);
    }

    /**
     * 克隆SSL配置
     *
     * @param config SSL配置
     * @return 克隆后的SSL配置
     */
    public static ShellSSLConfig clone(ShellSSLConfig config) {
        if (config == null) {
            return null;
        }
        ShellSSLConfig proxyConfig = new ShellSSLConfig();
        proxyConfig.copy(config);
        return proxyConfig;
    }
}
