package cn.oyzh.easyshell.domain;

import cn.oyzh.common.object.ObjectComparator;
import cn.oyzh.common.util.BooleanUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.store.jdbc.Column;
import cn.oyzh.store.jdbc.PrimaryKey;
import cn.oyzh.store.jdbc.Table;

import java.io.Serializable;

/**
 * @author oyzh
 * @since 2023/6/16
 */
@Table("t_connect")
public class ShellConnect implements Comparable<ShellConnect>, Serializable, ObjectComparator<ShellConnect> {

    /**
     * 数据id
     */
    @Column
    @PrimaryKey
    private String id;

    /**
     * 连接地址
     */
    @Column
    private String host;

    /**
     * 名称
     */
    @Column
    private String name;

    /**
     * 备注信息
     */
    @Column
    private String remark;

    /**
     * 分组id
     */
    @Column
    private String groupId;

    /**
     * 认证用户
     */
    @Column
    private String user;

    /**
     * 认证密码
     */
    @Column
    private String password;

    /**
     * 字符集
     */
    @Column
    private String charset;

    /**
     * 连接超时时间
     */
    @Column
    private Integer connectTimeOut;

    /**
     * 是否开启ssh转发
     */
    @Column
    private Boolean sshForward;

    /**
     * ssh信息
     */
    private ShellSSHConfig sshConfig;

    /**
     * x11转发
     */
    @Column
    private Boolean x11forwarding;

    /**
     * x11配置
     */
    private ShellX11Config x11Config;

    /**
     * 认证方式
     */
    private String authMethod;

    /**
     * 证书路径
     */
    private String certificatePath;

    public String getAuthMethod() {
        return authMethod;
    }

    public void setAuthMethod(String authMethod) {
        this.authMethod = authMethod;
    }

    public String getCertificatePath() {
        return certificatePath;
    }

    public void setCertificatePath(String certificatePath) {
        this.certificatePath = certificatePath;
    }

    public boolean isPasswordAuth() {
        return StringUtil.isBlank(this.authMethod) || StringUtil.equalsAnyIgnoreCase(this.authMethod, "password");
    }

    /**
     * 复制对象
     *
     * @param shellConnect shell信息
     * @return 当前对象
     */
    public ShellConnect copy(ShellConnect shellConnect) {
        this.name = shellConnect.name;
        this.host = shellConnect.host;
        this.user = shellConnect.user;
        this.remark = shellConnect.remark;
        this.groupId = shellConnect.groupId;
        this.password = shellConnect.password;
        this.charset = shellConnect.charset;
        this.connectTimeOut = shellConnect.connectTimeOut;
        // ssh
        this.sshConfig = shellConnect.sshConfig;
        this.sshForward = shellConnect.sshForward;
        // x11
        this.x11Config = shellConnect.x11Config;
        this.x11forwarding = shellConnect.x11forwarding;
        return this;
    }

    /**
     * 是否开启ssh转发
     *
     * @return 结果
     */
    public boolean isSSHForward() {
        return BooleanUtil.isTrue(this.sshForward);
    }

    public boolean isX11forwarding() {
        return this.x11forwarding != null && this.x11forwarding;
    }

    /**
     * 获取连接超时
     *
     * @return 连接超时
     */
    public Integer getConnectTimeOut() {
        return this.connectTimeOut == null || this.connectTimeOut < 3 ? 5 : this.connectTimeOut;
    }

    /**
     * 获取连接超时毫秒值
     *
     * @return 连接超时毫秒值
     */
    public int connectTimeOutMs() {
        return this.getConnectTimeOut() * 1000;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setConnectTimeOut(Integer connectTimeOut) {
        this.connectTimeOut = connectTimeOut;
    }

    public Boolean getSshForward() {
        return sshForward;
    }

    public void setSshForward(Boolean sshForward) {
        this.sshForward = sshForward;
    }

    public ShellSSHConfig getSshConfig() {
        return sshConfig;
    }

    public void setSshConfig(ShellSSHConfig sshConfig) {
        this.sshConfig = sshConfig;
    }

    public Boolean getX11forwarding() {
        return x11forwarding;
    }

    public void setX11forwarding(Boolean x11forwarding) {
        this.x11forwarding = x11forwarding;
    }

    public ShellX11Config getX11Config() {
        return x11Config;
    }

    public void setX11Config(ShellX11Config x11Config) {
        this.x11Config = x11Config;
    }

    public String getCharset() {
        return StringUtil.isBlank(this.charset) ? "utf-8" : charset;
    }

    public void setCharset(String charset) {
        this.charset = charset;
    }

    @Override
    public int compareTo(ShellConnect o) {
        if (o == null) {
            return 1;
        }
        return this.name.compareToIgnoreCase(o.getName());
    }

    /**
     * 获取连接ip
     *
     * @return 连接ip
     */
    public String hostIp() {
        if (StringUtil.isBlank(this.host)) {
            return "";
        }
        return this.host.split(":")[0];
    }

    /**
     * 获取连接端口
     *
     * @return 连接端口
     */
    public int hostPort() {
        if (StringUtil.isBlank(this.host)) {
            return -1;
        }
        try {
            return Integer.parseInt(this.host.split(":")[1]);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return -1;
    }

    @Override
    public boolean compare(ShellConnect t1) {
        if (t1 == null) {
            return false;
        }
        return StringUtil.equals(this.name, t1.name);
    }
}
