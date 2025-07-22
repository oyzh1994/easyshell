package cn.oyzh.easyshell.domain;

import cn.oyzh.common.file.FileUtil;
import cn.oyzh.common.object.ObjectComparator;
import cn.oyzh.common.object.ObjectCopier;
import cn.oyzh.common.util.BooleanUtil;
import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.common.util.ResourceUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.store.jdbc.Column;
import cn.oyzh.store.jdbc.PrimaryKey;
import cn.oyzh.store.jdbc.Table;
import com.alibaba.fastjson2.annotation.JSONField;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author oyzh
 * @since 2023/6/16
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
     * 连接超时时间
     */
    @Column
    private Integer connectTimeOut;

    /**
     * 跳板信息
     */
    private List<ShellJumpConfig> jumpConfigs;

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

    /**
     * 是否开启背景
     */
    @Column
    private Boolean enableBackground;

    /**
     * 背景图片
     */
    @Column
    private String backgroundImage;

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
     * ftp、vnc协议使用此字段
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
     */
    @Column
    private Boolean enableZModem;

    /**
     * s3协议，区域
     */
    @Column
    private String region;

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
     * s3协议，类型
     */
    @Column
    private String s3Type;

    /**
     * s3协议，appId
     */
    @Column
    private String s3AppId;

    public void setEnableCompress(boolean enableCompress) {
        this.enableCompress = enableCompress;
    }

    public boolean isEnableCompress() {
        return enableCompress != null && enableCompress;
    }

    public void setEnableZModem(boolean enableZModem) {
        this.enableZModem = enableZModem;
    }

    public boolean isEnableZModem() {
        return BooleanUtil.isTrue(enableZModem);
    }

    public void setEnableProxy(boolean enableProxy) {
        this.enableProxy = enableProxy;
    }

    public boolean isEnableProxy() {
        return enableProxy != null && enableProxy;
    }

    public ShellProxyConfig getProxyConfig() {
        return proxyConfig;
    }

    public void setProxyConfig(ShellProxyConfig proxyConfig) {
        this.proxyConfig = proxyConfig;
    }

    public String getKeyId() {
        return keyId;
    }

    public void setKeyId(String keyId) {
        this.keyId = keyId;
    }

    public boolean getEnableBackground() {
        return enableBackground;
    }

    public boolean isEnableBackground() {
        return enableBackground != null && enableBackground;
    }

    public void setEnableBackground(boolean enableBackground) {
        this.enableBackground = enableBackground;
    }

    public String getBackgroundImage() {
        return backgroundImage;
    }

    public String getBackgroundImageUrl() {
        // 处理图片
        if (!StringUtil.startWithAnyIgnoreCase(this.backgroundImage, "http", "https")) {
            return ResourceUtil.getLocalFileUrl(backgroundImage);
        }
        return backgroundImage;
    }

    public void setBackgroundImage(String backgroundImage) {
        this.backgroundImage = backgroundImage;
    }

    /**
     * 背景图片是否失效
     *
     * @return 结果
     */
    public boolean isBackgroundImageInvalid() {
        if (this.isEnableBackground()) {
            if (StringUtil.startWithAnyIgnoreCase(this.backgroundImage, "http", "https")) {
                return false;
            }
            if (FileUtil.exists(this.backgroundImage)) {
                return false;
            }
        }
        return true;
    }

    public String getOsType() {
        return osType;
    }

    public void setOsType(String osType) {
        this.osType = osType;
    }

    public String getAuthMethod() {
        return authMethod;
    }

    public void setAuthMethod(String authMethod) {
        this.authMethod = authMethod;
    }

    public String getCertificate() {
        return certificate;
    }

    public void setCertificate(String certificate) {
        this.certificate = certificate;
    }

    public boolean isPasswordAuth() {
        return StringUtil.isBlank(this.authMethod) || StringUtil.equalsIgnoreCase(this.authMethod, "password");
    }

    public boolean isCertificateAuth() {
        return StringUtil.equalsIgnoreCase(this.authMethod, "certificate");
    }

    public boolean isManagerAuth() {
        return StringUtil.equalsIgnoreCase(this.authMethod, "manager");
    }

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
        this.termType = t1.termType;
        this.connectTimeOut = t1.connectTimeOut;
        // ftp
        this.sslMode = t1.sslMode;
        this.ftpPassiveMode = t1.ftpPassiveMode;
        // s3
        this.region = t1.region;
        // ssh
        this.showFile = t1.showFile;
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
        // 背景
        this.backgroundImage = t1.backgroundImage;
        this.enableBackground = t1.enableBackground;
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
        this.s3Type = t1.s3Type;
        this.s3AppId = t1.s3AppId;
    }

    /**
     * 是否开启ssh跳板
     *
     * @return 结果
     */
    public boolean isJumpForward() {
        return CollectionUtil.isNotEmpty(this.jumpConfigs);
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

    public boolean getX11forwarding() {
        return x11forwarding;
    }

    public void setX11forwarding(boolean x11forwarding) {
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
        return StringUtil.equals(this.id, t1.id);
    }

    public String getTermType() {
        return StringUtil.isBlank(this.termType) ? "xterm" : this.termType;
        // return StringUtil.isBlank(this.termType) ? "xterm-256color" : this.termType;
    }

    public void setTermType(String termType) {
        this.termType = termType;
    }

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

    public void setTunnelingConfigs(List<ShellTunnelingConfig> tunnelingConfigs) {
        this.tunnelingConfigs = tunnelingConfigs;
    }

    public String getType() {
        return StringUtil.isBlank(this.type) ? "ssh" : type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @JSONField(serialize = false, deserialize = false)
    public boolean isSSHType() {
        return StringUtil.isBlank(this.type) || "ssh".equalsIgnoreCase(this.type);
    }

    @JSONField(serialize = false, deserialize = false)
    public boolean isLocalType() {
        return "local".equalsIgnoreCase(this.type);
    }

    @JSONField(serialize = false, deserialize = false)
    public boolean isTelnetType() {
        return "telnet".equalsIgnoreCase(this.type);
    }

    @JSONField(serialize = false, deserialize = false)
    public boolean isSerialType() {
        return "serial".equalsIgnoreCase(this.type);
    }

    @JSONField(serialize = false, deserialize = false)
    public boolean isSFTPType() {
        return "sftp".equalsIgnoreCase(this.type);
    }

    @JSONField(serialize = false, deserialize = false)
    public boolean isFTPType() {
        return "ftp".equalsIgnoreCase(this.type);
    }

    @JSONField(serialize = false, deserialize = false)
    public boolean isVNCType() {
        return "vnc".equalsIgnoreCase(this.type);
    }

    @JSONField(serialize = false, deserialize = false)
    public boolean isRloginType() {
        return "rlogin".equalsIgnoreCase(this.type);
    }

    @JSONField(serialize = false, deserialize = false)
    public boolean isS3Type() {
        return "s3".equalsIgnoreCase(this.type);
    }

    public int getSerialBaudRate() {
        return serialBaudRate;
    }

    public void setSerialBaudRate(int serialBaudRate) {
        this.serialBaudRate = serialBaudRate;
    }

    public String getSerialPortName() {
        return serialPortName;
    }

    public void setSerialPortName(String serialPortName) {
        this.serialPortName = serialPortName;
    }

    public int getSerialParityBits() {
        return serialParityBits;
    }

    public void setSerialParityBits(int serialParityBits) {
        this.serialParityBits = serialParityBits;
    }

    public int getSerialNumDataBits() {
        return serialNumDataBits;
    }

    public void setSerialNumDataBits(int serialNumDataBits) {
        this.serialNumDataBits = serialNumDataBits;
    }

    public int getSerialNumStopBits() {
        return serialNumStopBits;
    }

    public void setSerialNumStopBits(int serialNumStopBits) {
        this.serialNumStopBits = serialNumStopBits;
    }

    public int getSerialFlowControl() {
        return serialFlowControl;
    }

    public void setSerialFlowControl(int serialFlowControl) {
        this.serialFlowControl = serialFlowControl;
    }

    public boolean isSSLMode() {
        return BooleanUtil.isTrue(sslMode);
    }

    public void setSSLMode(boolean sslMode) {
        this.sslMode = sslMode;
    }

    public boolean isFtpPassiveMode() {
        return BooleanUtil.isTrue(ftpPassiveMode);
    }

    public void setFtpPassiveMode(boolean ftpPassiveMode) {
        this.ftpPassiveMode = ftpPassiveMode;
    }

    public String getEnvironment() {
        return environment;
    }

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
    public boolean isTermType() {
        return this.isSSHType() || this.isLocalType() || this.isTelnetType() || this.isSerialType() || this.isRloginType();
    }

    /**
     * 是否文件类型
     *
     * @return 结果
     */
    public boolean isFileType() {
        return this.isSSHType() || this.isSFTPType() || this.isFTPType() || this.isS3Type();
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getCertificatePwd() {
        return certificatePwd;
    }

    public void setCertificatePwd(String certificatePwd) {
        this.certificatePwd = certificatePwd;
    }

    public boolean isShowFile() {
        return this.showFile == null || this.showFile;
    }

    public void setShowFile(boolean showFile) {
        this.showFile = showFile;
    }

    public boolean isServerMonitor() {
        return BooleanUtil.isTrue(this.serverMonitor);
    }

    public void setServerMonitor(boolean serverMonitor) {
        this.serverMonitor = serverMonitor;
    }

    public boolean isFollowTerminalDir() {
        return BooleanUtil.isTrue(this.followTerminalDir);
    }

    public void setFollowTerminalDir(boolean followTerminalDir) {
        this.followTerminalDir = followTerminalDir;
    }

    public boolean isShowHiddenFile() {
        return BooleanUtil.isTrue(this.showHiddenFile);
    }

    public void setShowHiddenFile(boolean showHiddenFile) {
        this.showHiddenFile = showHiddenFile;
    }

    public String getS3Type() {
        return s3Type;
    }

    public void setS3Type(String s3Type) {
        this.s3Type = s3Type;
    }

    public String getS3AppId() {
        return s3AppId;
    }

    public void setS3AppId(String s3AppId) {
        this.s3AppId = s3AppId;
    }

    @JSONField(serialize = false, deserialize = false)
    public boolean isAlibabaS3Type(){
        return "alibaba".equalsIgnoreCase(this.s3Type);
    }

    @JSONField(serialize = false, deserialize = false)
    public boolean isHuaweiS3Type(){
        return "huawei".equalsIgnoreCase(this.s3Type);
    }

    @JSONField(serialize = false, deserialize = false)
    public boolean isTencentS3Type(){
        return "tencent".equalsIgnoreCase(this.s3Type);
    }

    @JSONField(serialize = false, deserialize = false)
    public boolean isMinioS3Type(){
        return "minio".equalsIgnoreCase(this.s3Type);
    }

    @JSONField(serialize = false, deserialize = false)
    public boolean isStandardS3Type(){
        return "s3".equalsIgnoreCase(this.s3Type);
    }
}
