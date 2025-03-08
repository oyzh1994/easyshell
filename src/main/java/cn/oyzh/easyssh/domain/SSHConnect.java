package cn.oyzh.easyssh.domain;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.store.jdbc.Column;
import cn.oyzh.store.jdbc.PrimaryKey;
import cn.oyzh.store.jdbc.Table;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author oyzh
 * @since 2023/6/16
 */
@Setter
@ToString
@Table("t_connect")
public class SSHConnect implements Comparable<SSHConnect>, Serializable {

    /**
     * 数据id
     */
    @Getter
    @Column
    @PrimaryKey
    private String id;

    /**
     * 连接地址
     */
    @Getter
    @Column
    private String host;

    /**
     * 名称
     */
    @Getter
    @Column
    private String name;

    /**
     * 备注信息
     */
    @Getter
    @Column
    private String remark;

    /**
     * 分组id
     */
    @Getter
    @Setter
    @Column
    private String groupId;

    /**
     * 认证用户
     */
    @Getter
    @Setter
    @Column
    private String user;

    /**
     * 认证密码
     */
    @Getter
    @Setter
    @Column
    private String password;

    /**
     * 连接超时时间
     */
    @Setter
    @Column
    private Integer connectTimeOut;

    /**
     * x11转发
     */
    @Getter
    @Column
    private Boolean x11forwarding;

    /**
     * x11配置
     */
    @Getter
    private SSHX11Config x11Config;

    /**
     * 复制对象
     *
     * @param sshConnect ssh信息
     * @return 当前对象
     */
    public SSHConnect copy(@NonNull SSHConnect sshConnect) {
        this.id = sshConnect.id;
        this.name = sshConnect.name;
        this.host = sshConnect.host;
        this.user = sshConnect.user;
        this.remark = sshConnect.remark;
        this.groupId = sshConnect.groupId;
        this.password = sshConnect.password;
        this.x11Config = sshConnect.x11Config;
        this.x11forwarding = sshConnect.x11forwarding;
        this.connectTimeOut = sshConnect.connectTimeOut;
        return this;
    }

    public  boolean isX11forwarding(){
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

    @Override
    public int compareTo(SSHConnect o) {
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
}
