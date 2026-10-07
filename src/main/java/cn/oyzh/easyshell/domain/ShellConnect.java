package cn.oyzh.easyshell.domain;

import cn.oyzh.common.json.JSONUtil;
import cn.oyzh.common.object.ObjectComparator;
import cn.oyzh.common.object.ObjectCopier;
import cn.oyzh.common.util.BooleanUtil;
import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.domain.zk.ShellZKSASLConfig;
import cn.oyzh.easyshell.internal.ShellPrototype;
import cn.oyzh.store.jdbc.Column;
import cn.oyzh.store.jdbc.PrimaryKey;
import cn.oyzh.store.jdbc.Table;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.annotation.JSONField;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * shell连接配置
 *
 * @author oyzh
 * @since 2023-08-16
 */
@Table("t_connect")
public class ShellConnect implements ObjectCopier<ShellConnect>, Comparable<ShellConnect>, Serializable, ObjectComparator<ShellConnect> {

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
     * 终端类型
     */
    @Column
    private String termType;

    /**
     * 终端退格类型
     */
    @Column
    private Integer backspaceType;

    /**
     * 终端alt修饰
     */
    @Column
    private Boolean altSendsEscape;

    /**
     * 连接超时时间
     */
    @Column
    private Integer connectTimeOut;

    /**
     * 跳板信息
     */
    private List<ShellJumpConfig> jumpConfigs;

    /**
     * 客户端转发
     */
    @Column
    private Boolean forwardAgent;

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
    @Column
    private String authMethod;

    /**
     * 证书路径
     */
    @Column
    private String certificate;

    /**
     * 证书密码
     */
    @Column
    private String certificatePwd;

    /**
     * 密钥id
     */
    @Column
    private String keyId;

    /**
     * 系统类型
     */
    @Column
    private String osType;

    //    /**
    //     * 是否开启背景
    //     */
    //    @Column
    //    private Boolean enableBackground;
    //
    //    /**
    //     * 背景图片
    //     */
    //    @Column
    //    private String backgroundImage;

    /**
     * 是否开启代理转发
     */
    @Column
    private Boolean enableProxy;

    /**
     * 代理配置
     */
    private ShellProxyConfig proxyConfig;

    /**
     * 隧道信息
     */
    private List<ShellTunnelingConfig> tunnelingConfigs;

    /**
     * 连接类型
     * ssh ssh
     * ftp ftp
     * sftp sftp
     * local 本地
     * serial 串口
     * telnet telnet
     * vnc nvc
     * smb smb
     * s3 s3
     * redis redis
     * zk zookeeper
     */
    @Column
    private String type;

    /**
     * 波特率-串口
     */
    @Column
    private int serialBaudRate;

    /**
     * 端口-串口
     */
    @Column
    private String serialPortName;

    /**
     * 校验位-串口
     */
    @Column
    private int serialParityBits;

    /**
     * 数据位-串口
     */
    @Column
    private int serialNumDataBits;

    /**
     * 停止位-串口
     */
    @Column
    private int serialNumStopBits;

    /**
     * 流控-串口
     */
    @Column
    private int serialFlowControl;

    /**
     * ssl模式
     * ftp、vnc、redis、mysql、rdp协议使用此字段
     */
    @Column
    private Boolean sslMode;

    /**
     * ftp的被动模式
     */
    @Column
    private Boolean ftpPassiveMode;

    /**
     * 环境信息
     */
    @Column
    private String environment;

    /**
     * 启用压缩
     */
    @Column
    private Boolean enableCompress;

    /**
     * ssh协议，启用ZModem
     * 默认启用
     */
    @Column
    private Boolean enableZModem;

    /**
     * ssh协议，显示文件
     */
    @Column
    private Boolean showFile;

    /**
     * ssh协议，显示服务监控
     */
    @Column
    private Boolean serverMonitor;

    /**
     * ssh协议，跟随终端目录
     */
    @Column
    private Boolean followTerminalDir;

    /**
     * ftp、sftp 是否显示隐藏文件
     */
    @Column
    private Boolean showHiddenFile;

    /**
     * s3协议，区域
     */
    @Column
    private String region;

    /**
     * s3协议，类型
     */
    @Column
    private String s3Type;

    //    /**
    //     * s3协议，appId
    //     */
    //    @Column
    //    private String s3AppId;

    /**
     * smb协议，共享名称
     */
    @Column
    private String smbShareName;

    /**
     * 域，smb、rdp协议
     */
    @Column
    private String domain;

    /**
     * 只读，zk、redis、vnc协议
     */
    @Column
    private Boolean readonly;

    /**
     * 执行超时，redis协议
     */
    @Column
    private Integer executeTimeOut;

    /**
     * ssl配置 redis
     */
    private ShellSSLConfig sslConfig;

    ///**
    // * 监听节点 zk协议
    // * false: 否
    // * null|true: 是
    // */
    //@Column
    // private Boolean listen;

    /**
     * 是否开启sasl认证 zk协议
     */
    @Column
    private Boolean saslAuth;

    /**
     * 会话超时时间 zk协议
     */
    @Column
    private Integer sessionTimeOut;

    /**
     * 兼容模式 zk协议
     * null: 无
     * 1: 兼容3.4.x版本
     */
    @Column
    private Integer compatibility;

    //    /**
    //     * 认证列表 zk协议
    //     */
    //    private List<ShellZKAuth> auths;

    /**
     * sasl配置
     */
    private ShellZKSASLConfig saslConfig;
    //
    //    /**
    //     * 分辨率，rdp
    //     */
    //    private String resolution;

    // ==================== MongoDB 专属字段 ====================

    //    /**
    //     * MongoDB 认证方式 (password/x509)
    //     */
    //    @Column
    //    private String mongoAuthType;

    /**
     * MongoDB 认证数据库
     */
    @Column
    private String mongoAuthDatabase;

    /**
     * MongoDB 指定数据库
     */
    @Column
    private String mongoSpecifiedDatabase;

    /**
     * Mosh认证key
     */
    @Column
    private String moshKey;

    /**
     * 收藏列表
     */
    private List<String> collects;

    /**
     * 扩展内容
     */
    @Column
    private String extras;

    /** 设置是否启用压缩 */
    public void setEnableCompress(boolean enableCompress) {
        this.enableCompress = enableCompress;
    }

    /** 是否启用压缩 */
    public boolean isEnableCompress() {
        return enableCompress == null || this.enableCompress;
    }

    /** 设置是否启用ZModem */
    public void setEnableZModem(boolean enableZModem) {
        this.enableZModem = enableZModem;
    }

    /** 是否启用ZModem */
    public boolean isEnableZModem() {
        // BooleanUtil.isTrue(enableZModem);
        return enableZModem == null || enableZModem;
    }

    /** 设置是否启用代理转发 */
    public void setEnableProxy(boolean enableProxy) {
        this.enableProxy = enableProxy;
    }

    /** 是否启用代理转发 */
    public boolean isEnableProxy() {
        return enableProxy != null && enableProxy;
    }

    /** 获取代理配置 */
    public ShellProxyConfig getProxyConfig() {
        return proxyConfig;
    }

    /** 设置代理配置 */
    public void setProxyConfig(ShellProxyConfig proxyConfig) {
        this.proxyConfig = proxyConfig;
    }

    /** 获取密钥id */
    public String getKeyId() {
        return keyId;
    }

    /** 设置密钥id */
    public void setKeyId(String keyId) {
        this.keyId = keyId;
    }

    // public boolean getEnableBackground() {
    //     return enableBackground;
    // }

    //    public boolean isEnableBackground() {
    //        return enableBackground != null && enableBackground;
    //    }
    //
    //    public void setEnableBackground(boolean enableBackground) {
    //        this.enableBackground = enableBackground;
    //    }
    //
    //    public String getBackgroundImage() {
    //        return backgroundImage;
    //    }
    //
    //    @JSONField(serialize = false, deserialize = false)
    //    public String getBackgroundImageUrl() {
    //        // 处理图片
    //        if (StringUtil.isNotBlank(this.backgroundImage) && !StringUtil.startWithAnyIgnoreCase(this.backgroundImage, "http", "https")) {
    //            return ResourceUtil.getLocalFileUrl(backgroundImage);
    //        }
    //        return backgroundImage;
    //    }
    //
    //    public void setBackgroundImage(String backgroundImage) {
    //        this.backgroundImage = backgroundImage;
    //    }

    //    /**
    //     * 背景图片是否失效
    //     *
    //     * @return 结果
    //     */
    //    @JSONField(serialize = false, deserialize = false)
    //    public boolean isBackgroundImageInvalid() {
    //        if (this.isEnableBackground()) {
    //            if (StringUtil.startWithAnyIgnoreCase(this.backgroundImage, "http", "https")) {
    //                return false;
    //            }
    //            if (FileUtil.exists(this.backgroundImage)) {
    //                return false;
    //            }
    //        }
    //        return true;
    //    }

    /** 获取系统类型 */
    public String getOsType() {
        return osType;
    }

    /** 设置系统类型 */
    public void setOsType(String osType) {
        this.osType = osType;
    }

    /** 获取认证方式 */
    public String getAuthMethod() {
        return authMethod;
    }

    /** 设置认证方式 */
    public void setAuthMethod(String authMethod) {
        this.authMethod = authMethod;
    }

    /** 获取证书路径 */
    public String getCertificate() {
        return certificate;
    }

    /** 设置证书路径 */
    public void setCertificate(String certificate) {
        this.certificate = certificate;
    }

    /**
     * 是否密码认证
     *
     * @return 结果
     */
    @JSONField(serialize = false, deserialize = false)
    public boolean isPasswordAuth() {
        return StringUtil.isBlank(this.authMethod) || StringUtil.equalsIgnoreCase(this.authMethod, "password");
    }

    /**
     * 是否证书认证
     *
     * @return 结果
     */
    @JSONField(serialize = false, deserialize = false)
    public boolean isCertificateAuth() {
        return StringUtil.equalsIgnoreCase(this.authMethod, "certificate");
    }

    /**
     * 是否管理员认证
     *
     * @return 结果
     */
    @JSONField(serialize = false, deserialize = false)
    public boolean isManagerAuth() {
        return StringUtil.equalsIgnoreCase(this.authMethod, "manager");
    }

    /**
     * 是否SSH代理认证
     *
     * @return 结果
     */
    @JSONField(serialize = false, deserialize = false)
    public boolean isSSHAgentAuth() {
        return StringUtil.equalsIgnoreCase(this.authMethod, "sshAgent");
    }

    @Override
    public void copy(ShellConnect t1) {
        // 基本
        this.name = t1.name;
        this.host = t1.host;
        this.user = t1.user;
        this.type = t1.type;
        this.remark = t1.remark;
        this.osType = t1.osType;
        this.groupId = t1.groupId;
        this.charset = t1.charset;
        this.connectTimeOut = t1.connectTimeOut;
        // 终端
        this.backspaceType = t1.backspaceType;
        this.altSendsEscape = t1.altSendsEscape;
        // ftp
        this.ftpPassiveMode = t1.ftpPassiveMode;
        // s3
        this.region = t1.region;
        // ssh
        this.showFile = t1.showFile;
        this.termType = t1.termType;
        this.environment = t1.environment;
        this.enableZModem = t1.enableZModem;
        this.serverMonitor = t1.serverMonitor;
        this.enableCompress = t1.enableCompress;
        this.followTerminalDir = t1.followTerminalDir;
        // sftp
        this.showHiddenFile = t1.showHiddenFile;
        // 认证
        this.keyId = t1.keyId;
        this.password = t1.password;
        this.authMethod = t1.authMethod;
        this.certificate = t1.certificate;
        // 跳板机
        this.jumpConfigs = ShellJumpConfig.clone(t1.jumpConfigs);
        // 隧道
        this.tunnelingConfigs = ShellTunnelingConfig.clone(t1.tunnelingConfigs);
        // x11
        this.x11forwarding = t1.x11forwarding;
        this.x11Config = ShellX11Config.clone(t1.x11Config);
        //        // 背景
        //        this.backgroundImage = t1.backgroundImage;
        //        this.enableBackground = t1.enableBackground;
        // 代理
        this.enableProxy = t1.enableProxy;
        this.proxyConfig = ShellProxyConfig.clone(t1.proxyConfig);
        // 串口
        this.serialBaudRate = t1.serialBaudRate;
        this.serialPortName = t1.serialPortName;
        this.serialParityBits = t1.serialParityBits;
        this.serialNumDataBits = t1.serialNumDataBits;
        this.serialNumStopBits = t1.serialNumStopBits;
        this.serialFlowControl = t1.serialFlowControl;
        // s3
        this.region = t1.region;
        this.s3Type = t1.s3Type;
        //        this.s3AppId = t1.s3AppId;
        // smb
        this.domain = t1.domain;
        this.smbShareName = t1.smbShareName;
        // ssl
        this.sslMode = t1.sslMode;
        // 只读模式
        this.readonly = t1.readonly;
        // redis
        this.executeTimeOut = t1.executeTimeOut;
        this.sslConfig = ShellSSLConfig.clone(t1.sslConfig);
        // zk
        // this.listen = t1.listen;
        this.saslAuth = t1.saslAuth;
        this.compatibility = t1.compatibility;
        this.sessionTimeOut = t1.sessionTimeOut;
        //        this.auths = ShellZKAuth.clone(t1.auths);
        this.saslConfig = ShellZKSASLConfig.clone(t1.saslConfig);
        // mongo
        //        this.resolution = t1.resolution;
        this.mongoAuthDatabase = t1.mongoAuthDatabase;
        this.mongoSpecifiedDatabase = t1.mongoSpecifiedDatabase;
        // mosh
        this.moshKey = t1.moshKey;
        // 扩展字段
        this.extras = t1.extras;
    }

    /**
     * 是否开启ssh跳板
     *
     * @return 结果
     */
    public boolean isJumpForward() {
        return CollectionUtil.isNotEmpty(this.jumpConfigs);
    }

    /** 是否x11转发 */
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

    /** 获取数据id */
    public String getId() {
        return id;
    }

    /** 设置数据id */
    public void setId(String id) {
        this.id = id;
    }

    /** 获取连接地址 */
    public String getHost() {
        return host;
    }

    /** 设置连接地址 */
    public void setHost(String host) {
        this.host = host.trim();
    }

    /** 获取名称 */
    public String getName() {
        return name;
    }

    /** 设置名称 */
    public void setName(String name) {
        this.name = name;
    }

    /** 获取备注信息 */
    public String getRemark() {
        return remark;
    }

    /** 设置备注信息 */
    public void setRemark(String remark) {
        this.remark = remark;
    }

    /** 获取分组id */
    public String getGroupId() {
        return groupId;
    }

    /** 设置分组id */
    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    /** 获取认证用户 */
    public String getUser() {
        return user;
    }

    /** 设置认证用户 */
    public void setUser(String user) {
        this.user = user;
    }

    /** 获取认证密码 */
    public String getPassword() {
        return password;
    }

    /** 设置认证密码 */
    public void setPassword(String password) {
        this.password = password;
    }

    /** 设置连接超时时间 */
    public void setConnectTimeOut(Integer connectTimeOut) {
        this.connectTimeOut = connectTimeOut;
    }

    // public Boolean getX11forwarding() {
    //     return x11forwarding;
    // }

    /** 设置是否x11转发 */
    public void setX11forwarding(boolean x11forwarding) {
        this.x11forwarding = x11forwarding;
    }

    /** 获取x11配置 */
    public ShellX11Config getX11Config() {
        return x11Config;
    }

    /** 设置x11配置 */
    public void setX11Config(ShellX11Config x11Config) {
        this.x11Config = x11Config;
    }

    /** 获取字符集 */
    public String getCharset() {
        return StringUtil.isBlank(this.charset) ? "utf-8" : charset;
    }

    /** 设置字符集 */
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
        return StringUtil.equals(this.id, t1.id);
    }

    /** 获取终端类型 */
    public String getTermType() {
        return StringUtil.isBlank(this.termType) ? "xterm" : this.termType;
        // return StringUtil.isBlank(this.termType) ? "xterm-256color" : this.termType;
    }

    /** 设置终端类型 */
    public void setTermType(String termType) {
        this.termType = termType;
    }

    /** 获取跳板信息 */
    public List<ShellJumpConfig> getJumpConfigs() {
        return jumpConfigs;
    }

    /**
     * 获取启用的跳板机配置
     *
     * @return 启用的跳板机配置
     */
    @JSONField(serialize = false, deserialize = false)
    public List<ShellJumpConfig> getEnableJumpConfigs() {
        if (CollectionUtil.isEmpty(jumpConfigs)) {
            return Collections.emptyList();
        }
        return jumpConfigs.parallelStream().filter(ShellJumpConfig::isEnabled).toList();
    }

    /** 设置跳板信息 */
    public void setJumpConfigs(List<ShellJumpConfig> jumpConfigs) {
        this.jumpConfigs = jumpConfigs;
    }

    /**
     * 是否开启跳板
     *
     * @return 结果
     */
    @JSONField(serialize = false, deserialize = false)
    public boolean isEnableJump() {
        return CollectionUtil.isNotEmpty(this.getEnableJumpConfigs());
    }

    /** 获取隧道信息 */
    public List<ShellTunnelingConfig> getTunnelingConfigs() {
        return tunnelingConfigs;
    }

    /**
     * 获取启用的隧道转发配置
     *
     * @return 启用的隧道转发配置
     */
    @JSONField(serialize = false, deserialize = false)
    public List<ShellTunnelingConfig> getEnableTunnelingConfigs() {
        if (CollectionUtil.isEmpty(tunnelingConfigs)) {
            return Collections.emptyList();
        }
        return tunnelingConfigs.parallelStream().filter(ShellTunnelingConfig::isEnabled).toList();
    }

    /**
     * 是否开启隧道转发
     *
     * @return 结果
     */
    @JSONField(serialize = false, deserialize = false)
    public boolean isEnableTunneling() {
        return CollectionUtil.isNotEmpty(this.getEnableTunnelingConfigs());
    }

    /** 设置隧道信息 */
    public void setTunnelingConfigs(List<ShellTunnelingConfig> tunnelingConfigs) {
        this.tunnelingConfigs = tunnelingConfigs;
    }

    /** 获取连接类型 */
    public String getType() {
        return StringUtil.isBlank(this.type) ? "ssh" : type;
    }

    /** 设置连接类型 */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * 是否SSH类型
     *
     * @return 结果
     */
    @JSONField(serialize = false, deserialize = false)
    public boolean isSSHType() {
        return StringUtil.isBlank(this.type) || "ssh".equalsIgnoreCase(this.type);
    }

    /**
     * 是否本地类型
     *
     * @return 结果
     */
    @JSONField(serialize = false, deserialize = false)
    public boolean isLocalType() {
        return "local".equalsIgnoreCase(this.type);
    }

    /**
     * 是否Telnet类型
     *
     * @return 结果
     */
    @JSONField(serialize = false, deserialize = false)
    public boolean isTelnetType() {
        return "telnet".equalsIgnoreCase(this.type);
    }

    /**
     * 是否串口类型
     *
     * @return 结果
     */
    @JSONField(serialize = false, deserialize = false)
    public boolean isSerialType() {
        return "serial".equalsIgnoreCase(this.type);
    }

    /**
     * 是否SFTP类型
     *
     * @return 结果
     */
    @JSONField(serialize = false, deserialize = false)
    public boolean isSFTPType() {
        return "sftp".equalsIgnoreCase(this.type);
    }

    /**
     * 是否FTP类型
     *
     * @return 结果
     */
    @JSONField(serialize = false, deserialize = false)
    public boolean isFTPType() {
        return "ftp".equalsIgnoreCase(this.type);
    }

    /**
     * 是否VNC类型
     *
     * @return 结果
     */
    @JSONField(serialize = false, deserialize = false)
    public boolean isVNCType() {
        return "vnc".equalsIgnoreCase(this.type);
    }

    /**
     * 是否Rlogin类型
     *
     * @return 结果
     */
    @JSONField(serialize = false, deserialize = false)
    public boolean isRloginType() {
        return "rlogin".equalsIgnoreCase(this.type);
    }

    /**
     * 是否S3类型
     *
     * @return 结果
     */
    @JSONField(serialize = false, deserialize = false)
    public boolean isS3Type() {
        return "s3".equalsIgnoreCase(this.type);
    }

    /**
     * 是否SMB类型
     *
     * @return 结果
     */
    @JSONField(serialize = false, deserialize = false)
    public boolean isSMBType() {
        return "smb".equalsIgnoreCase(this.type);
    }

    /**
     * 是否Redis类型
     *
     * @return 结果
     */
    @JSONField(serialize = false, deserialize = false)
    public boolean isRedisType() {
        return "redis".equalsIgnoreCase(this.type);
    }

    /**
     * 是否Zookeeper类型
     *
     * @return 结果
     */
    @JSONField(serialize = false, deserialize = false)
    public boolean isZKType() {
        return StringUtil.equalsAnyIgnoreCase(this.type, "zookeeper", "zk");
    }

    /**
     * 是否RDP类型
     *
     * @return 结果
     */
    @JSONField(serialize = false, deserialize = false)
    public boolean isRDPType() {
        return StringUtil.equalsAnyIgnoreCase(this.type, "rdp");
    }

    /**
     * 是否Webdav类型
     *
     * @return 结果
     */
    @JSONField(serialize = false, deserialize = false)
    public boolean isWebdavType() {
        return StringUtil.equalsAnyIgnoreCase(this.type, ShellPrototype.WEBDAV);
    }

    /**
     * 是否Mysql类型
     *
     * @return 结果
     */
    @JSONField(serialize = false, deserialize = false)
    public boolean isMysqlType() {
        return StringUtil.equalsAnyIgnoreCase(this.type, ShellPrototype.MYSQL);
    }

    /**
     * 是否达梦类型
     *
     * @return 结果
     */
    @JSONField(serialize = false, deserialize = false)
    public boolean isDamengType() {
        return StringUtil.equalsAnyIgnoreCase(this.type, ShellPrototype.DAMENG);
    }

    /** 获取波特率-串口 */
    public int getSerialBaudRate() {
        return serialBaudRate;
    }

    /** 设置波特率-串口 */
    public void setSerialBaudRate(int serialBaudRate) {
        this.serialBaudRate = serialBaudRate;
    }

    /** 获取端口-串口 */
    public String getSerialPortName() {
        return serialPortName;
    }

    /** 设置端口-串口 */
    public void setSerialPortName(String serialPortName) {
        this.serialPortName = serialPortName;
    }

    /** 获取校验位-串口 */
    public int getSerialParityBits() {
        return serialParityBits;
    }

    /** 设置校验位-串口 */
    public void setSerialParityBits(int serialParityBits) {
        this.serialParityBits = serialParityBits;
    }

    /** 获取数据位-串口 */
    public int getSerialNumDataBits() {
        return serialNumDataBits;
    }

    /** 设置数据位-串口 */
    public void setSerialNumDataBits(int serialNumDataBits) {
        this.serialNumDataBits = serialNumDataBits;
    }

    /** 获取停止位-串口 */
    public int getSerialNumStopBits() {
        return serialNumStopBits;
    }

    /** 设置停止位-串口 */
    public void setSerialNumStopBits(int serialNumStopBits) {
        this.serialNumStopBits = serialNumStopBits;
    }

    /** 获取流控-串口 */
    public int getSerialFlowControl() {
        return serialFlowControl;
    }

    /** 设置流控-串口 */
    public void setSerialFlowControl(int serialFlowControl) {
        this.serialFlowControl = serialFlowControl;
    }

    /** 是否ssl模式 */
    public boolean isSSLMode() {
        return BooleanUtil.isTrue(sslMode);
    }

    /** 设置ssl模式 */
    public void setSSLMode(boolean sslMode) {
        this.sslMode = sslMode;
    }

    /** 是否ftp被动模式 */
    public boolean isFtpPassiveMode() {
        return BooleanUtil.isTrue(ftpPassiveMode);
    }

    /** 设置ftp被动模式 */
    public void setFtpPassiveMode(boolean ftpPassiveMode) {
        this.ftpPassiveMode = ftpPassiveMode;
    }

    /** 获取环境信息 */
    public String getEnvironment() {
        return environment;
    }

    /** 设置环境信息 */
    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    /**
     * 获取环境值
     *
     * @return 结果
     */
    public Map<String, String> environments() {
        Map<String, String> map = new HashMap<>();
        if (StringUtil.isBlank(this.environment)) {
            return map;
        }
        this.environment.lines().forEach(line -> {
            String[] arr = line.split("=");
            if (arr.length != 2) {
                return;
            }
            String key = arr[0].trim();
            String value = arr[1].trim();
            map.put(key, value);
        });
        return map;
    }

    // @Override
    // public boolean equals(Object obj) {
    //     if (obj instanceof ShellConnect connect && StringUtil.equals(connect.getId(), this.getId())) {
    //         return true;
    //     }
    //     return super.equals(obj);
    // }

    /**
     * 是否终端类型
     *
     * @return 结果
     */
    @JSONField(serialize = false, deserialize = false)
    public boolean isTermType() {
        return this.isSSHType() || this.isLocalType() || this.isTelnetType() || this.isSerialType() || this.isRloginType() || this.isMoshType();
    }

    /**
     * 是否文件类型
     *
     * @return 结果
     */
    @JSONField(serialize = false, deserialize = false)
    public boolean isFileType() {
        return this.isSSHType()
                || this.isSFTPType()
                || this.isFTPType()
                || this.isS3Type()
                || this.isSMBType()
                || this.isWebdavType();
    }

    /** 获取s3区域 */
    public String getRegion() {
        return region;
    }

    /** 设置s3区域 */
    public void setRegion(String region) {
        this.region = region;
    }

    /** 获取证书密码 */
    public String getCertificatePwd() {
        return certificatePwd;
    }

    /** 设置证书密码 */
    public void setCertificatePwd(String certificatePwd) {
        this.certificatePwd = certificatePwd;
    }

    /** 是否显示文件 */
    public boolean isShowFile() {
        return this.showFile == null || this.showFile;
    }

    /** 设置是否显示文件 */
    public void setShowFile(boolean showFile) {
        this.showFile = showFile;
    }

    /** 是否显示服务监控 */
    public boolean isServerMonitor() {
        return BooleanUtil.isTrue(this.serverMonitor);
    }

    /** 设置是否显示服务监控 */
    public void setServerMonitor(boolean serverMonitor) {
        this.serverMonitor = serverMonitor;
    }

    /** 是否跟随终端目录 */
    public boolean isFollowTerminalDir() {
        return BooleanUtil.isTrue(this.followTerminalDir);
    }

    /** 设置是否跟随终端目录 */
    public void setFollowTerminalDir(boolean followTerminalDir) {
        this.followTerminalDir = followTerminalDir;
    }

    /** 是否显示隐藏文件 */
    public boolean isShowHiddenFile() {
        return BooleanUtil.isTrue(this.showHiddenFile);
    }

    /** 设置是否显示隐藏文件 */
    public void setShowHiddenFile(boolean showHiddenFile) {
        this.showHiddenFile = showHiddenFile;
    }

    /** 获取s3类型 */
    public String getS3Type() {
        return s3Type;
    }

    /** 设置s3类型 */
    public void setS3Type(String s3Type) {
        this.s3Type = s3Type;
    }

    //    public String getS3AppId() {
    //        return s3AppId;
    //    }
    //
    //    public void setS3AppId(String s3AppId) {
    //        this.s3AppId = s3AppId;
    //    }

    /**
     * 是否阿里云S3类型
     *
     * @return 结果
     */
    @JSONField(serialize = false, deserialize = false)
    public boolean isAlibabaS3Type() {
        return "alibaba".equalsIgnoreCase(this.s3Type) || StringUtil.endsWith(this.host, ".aliyuncs.com");
    }

    /**
     * 是否华为云S3类型
     *
     * @return 结果
     */
    @JSONField(serialize = false, deserialize = false)
    public boolean isHuaweiS3Type() {
        return "huawei".equalsIgnoreCase(this.s3Type) || StringUtil.endsWith(this.host, ".myhuaweicloud.com");
    }

    /**
     * 是否腾讯云S3类型
     *
     * @return 结果
     */
    @JSONField(serialize = false, deserialize = false)
    public boolean isTencentS3Type() {
        return "tencent".equalsIgnoreCase(this.s3Type) || StringUtil.endsWith(this.host, ".myqcloud.com");
    }

    /**
     * 是否Minio S3类型
     *
     * @return 结果
     */
    @JSONField(serialize = false, deserialize = false)
    public boolean isMinioS3Type() {
        return "minio".equalsIgnoreCase(this.s3Type);
    }

    /**
     * 是否标准S3类型
     *
     * @return 结果
     */
    @JSONField(serialize = false, deserialize = false)
    public boolean isStandardS3Type() {
        return "s3".equalsIgnoreCase(this.s3Type);
    }

    /** 获取smb共享名称 */
    public String getSmbShareName() {
        return smbShareName;
    }

    /** 设置smb共享名称 */
    public void setSmbShareName(String smbShareName) {
        this.smbShareName = smbShareName;
    }

    /** 是否启用客户端转发 */
    public boolean isForwardAgent() {
        return BooleanUtil.isTrue(this.forwardAgent);
    }

    /** 设置是否启用客户端转发 */
    public void setForwardAgent(boolean forwardAgent) {
        this.forwardAgent = forwardAgent;
    }

    /** 是否只读 */
    public boolean isReadonly() {
        return BooleanUtil.isTrue(this.readonly);
    }

    /** 设置是否只读 */
    public void setReadonly(boolean readonly) {
        this.readonly = readonly;
    }

    /** 获取执行超时 */
    public Integer getExecuteTimeOut() {
        return this.executeTimeOut == null ? 3 : this.executeTimeOut;
    }

    /** 设置执行超时 */
    public void setExecuteTimeOut(Integer executeTimeOut) {
        this.executeTimeOut = executeTimeOut;
    }

    /**
     * 获取执行超时毫秒值
     *
     * @return 执行超时毫秒值
     */
    public int executeTimeOutMs() {
        return this.getExecuteTimeOut() * 1000;
    }

    /** 获取ssl配置 */
    public ShellSSLConfig getSslConfig() {
        return sslConfig;
    }

    /** 设置ssl配置 */
    public void setSslConfig(ShellSSLConfig sslConfig) {
        this.sslConfig = sslConfig;
    }

    /** 设置是否开启sasl认证 */
    public void setSaslAuth(Boolean saslAuth) {
        this.saslAuth = saslAuth;
    }

    /**
     * 是否开启sasl认证
     *
     * @return 结果
     */
    public boolean isSASLAuth() {
        return BooleanUtil.isTrue(this.saslAuth);
    }

    /** 获取兼容模式 */
    public Integer getCompatibility() {
        return compatibility;
    }

    /** 设置兼容模式 */
    public void setCompatibility(Integer compatibility) {
        this.compatibility = compatibility;
    }

    /**
     * 是否兼容3.4.x版本
     *
     * @return 结果
     */
    public boolean compatibility34() {
        return Objects.equals(1, this.compatibility);
    }

    // public void setListen(boolean listen) {
    //    this.listen = listen;
    //}
    //

    /// **
    // * 是否开启sasl认证
    // *
    // * @return 结果
    // */
    // public boolean isListen() {
    //    return BooleanUtil.isTrue(this.listen);
    //}
    //    public List<ShellZKAuth> getAuths() {
    //        return auths;
    //    }
    //
    //    public void setAuths(List<ShellZKAuth> auths) {
    //        this.auths = auths;
    //    }
    //
    //    public void addAuth(ShellZKAuth auth) {
    //        if (auth == null) {
    //            return;
    //        }
    //        if (this.auths == null) {
    //            this.auths = new ArrayList<>();
    //        } else {
    //            for (ShellZKAuth zkAuth : auths) {
    //                if (zkAuth.compare(auth)) {
    //                    return;
    //                }
    //            }
    //        }
    //        this.auths.add(auth);
    //    }

    /**
     * 获取会话超时
     *
     * @return 会话超时时间
     */
    public Integer getSessionTimeOut() {
        return this.sessionTimeOut == null || this.sessionTimeOut < 1 ? 30 : this.sessionTimeOut;
    }

    /**
     * 获取会话超时毫秒值
     *
     * @return 会话超时时间毫秒值
     */
    public int sessionTimeOutMs() {
        return this.getSessionTimeOut() * 60 * 1000;
    }

    /** 获取sasl配置 */
    public ShellZKSASLConfig getSaslConfig() {
        return saslConfig;
    }

    /** 设置sasl配置 */
    public void setSaslConfig(ShellZKSASLConfig saslConfig) {
        this.saslConfig = saslConfig;
    }

    /** 获取终端退格类型 */
    public Integer getBackspaceType() {
        return backspaceType;
    }

    /** 设置终端退格类型 */
    public void setBackspaceType(Integer backspaceType) {
        this.backspaceType = backspaceType;
    }

    /** 是否终端alt修饰 */
    public boolean isAltSendsEscape() {
        return BooleanUtil.isTrue(this.altSendsEscape);
    }

    /** 设置终端alt修饰 */
    public void setAltSendsEscape(boolean altSendsEscape) {
        this.altSendsEscape = altSendsEscape;
    }

    /** 获取域 */
    public String getDomain() {
        return domain;
    }

    /** 设置域 */
    public void setDomain(String domain) {
        this.domain = domain;
    }

    // ==================== MongoDB 专属方法 ====================

    /**
     * 是否MongoDB类型
     *
     * @return 结果
     */
    @JSONField(serialize = false, deserialize = false)
    public boolean isMongoType() {
        return StringUtil.equalsAnyIgnoreCase(this.type, ShellPrototype.MONGO);
    }

    //    public String getMongoAuthType() {
    //        return mongoAuthType;
    //    }
    //
    //    public void setMongoAuthType(String mongoAuthType) {
    //        this.mongoAuthType = mongoAuthType;
    //    }

    /** 获取MongoDB认证数据库 */
    public String getMongoAuthDatabase() {
        return mongoAuthDatabase;
    }

    /** 设置MongoDB认证数据库 */
    public void setMongoAuthDatabase(String mongoAuthDatabase) {
        this.mongoAuthDatabase = mongoAuthDatabase;
    }

    /** 获取MongoDB指定数据库 */
    public String getMongoSpecifiedDatabase() {
        return mongoSpecifiedDatabase;
    }

    /** 设置MongoDB指定数据库 */
    public void setMongoSpecifiedDatabase(String mongoSpecifiedDatabase) {
        this.mongoSpecifiedDatabase = mongoSpecifiedDatabase;
    }

    /**
     * 获取MongoDB指定数据库集合
     *
     * @return 指定数据库集合
     */
    @JSONField(serialize = false, deserialize = false)
    public Set<String> mongoSpecifiedDatabases() {
        if (StringUtil.isBlank(this.mongoSpecifiedDatabase)) {
            return Collections.emptySet();
        }
        Set<String> set = new HashSet<>();
        String[] arr = this.mongoSpecifiedDatabase.split(",");
        for (String s : arr) {
            if (StringUtil.isNotBlank(s)) {
                set.add(s);
            }
        }
        return set;
    }

    /**
     * 是否Mosh类型
     *
     * @return 结果
     */
    @JSONField(serialize = false, deserialize = false)
    public boolean isMoshType() {
        return StringUtil.equalsAnyIgnoreCase(this.type, ShellPrototype.MOSH);
    }

    /** 获取Mosh认证key */
    public String getMoshKey() {
        return moshKey;
    }

    /** 设置Mosh认证key */
    public void setMoshKey(String moshKey) {
        this.moshKey = moshKey;
    }

    /** 获取收藏列表 */
    public List<String> getCollects() {
        return collects;
    }

    /** 设置收藏列表 */
    public void setCollects(List<String> collects) {
        this.collects = collects;
    }

    /**
     * 是否已收藏指定路径
     *
     * @param path 路径
     * @return 结果
     */
    public boolean isCollect(String path) {
        return CollectionUtil.isNotEmpty(this.collects) && this.collects.contains(path);
    }

    /**
     * 添加收藏
     *
     * @param path 路径
     */
    public void addCollect(String path) {
        if (this.collects == null) {
            this.collects = new ArrayList<>();
        }
        if (!this.collects.contains(path)) {
            this.collects.add(path);
        }
    }

    /**
     * 移除收藏
     *
     * @param path 路径
     * @return 结果
     */
    public boolean removeCollect(String path) {
        if (this.collects != null) {
            return this.collects.remove(path);
        }
        return false;
    }

    //    public boolean isMongoPasswordAuth() {
    //        return "password".equalsIgnoreCase(this.mongoAuthType);
    //    }

    /** 获取扩展内容 */
    public String getExtras() {
        return extras;
    }

    /** 设置扩展内容 */
    public void setExtras(String extras) {
        this.extras = extras;
    }

    /**
     * 获取扩展内容json对象
     *
     * @return 扩展内容json对象
     */
    private JSONObject extrasJson() {
        JSONObject json;
        try {
            if (StringUtil.isBlank(this.extras)) {
                json = new JSONObject();
            } else {
                json = JSONUtil.parseObject(this.extras);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            json = new JSONObject();
        }
        return json;
    }

    /**
     * 写入扩展内容
     *
     * @param key   键
     * @param value 值
     */
    public void putExtra(String key, Object value) {
        JSONObject json = this.extrasJson();
        json.put(key, value);
        this.extras = json.toString();
    }

    /**
     * 是否包含扩展内容
     *
     * @param key 键
     * @return 结果
     */
    public boolean containsExtra(String key) {
        JSONObject json = this.extrasJson();
        return json.containsKey(key);
    }

    /**
     * 获取扩展内容
     *
     * @param key 键
     * @return 扩展内容
     */
    public <T> T getExtra(String key) {
        JSONObject json = this.extrasJson();
        return (T) json.get(key);
    }
}
