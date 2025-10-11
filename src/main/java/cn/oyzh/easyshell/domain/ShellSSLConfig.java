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
     * 客户端证书
     */
    @Column
    private String clientCrt;

    /**
     * ca证书
     */
    @Column
    private String caCrt;

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

    public String getClientKey() {
        return clientKey;
    }

    public void setClientKey(String clientKey) {
        this.clientKey = clientKey;
    }

    public String getClientCrt() {
        return clientCrt;
    }

    public void setClientCrt(String clientCrt) {
        this.clientCrt = clientCrt;
    }

    public String getCaCrt() {
        return caCrt;
    }

    public void setCaCrt(String caCrt) {
        this.caCrt = caCrt;
    }

    @Override
    public void copy(ShellSSLConfig t1) {
        this.caCrt = t1.caCrt;
        this.clientCrt = t1.clientCrt;
        this.clientKey = t1.clientKey;
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

    public static ShellSSLConfig clone(ShellSSLConfig config) {
        if (config == null) {
            return null;
        }
        ShellSSLConfig proxyConfig = new ShellSSLConfig();
        proxyConfig.copy(config);
        return proxyConfig;
    }
}
