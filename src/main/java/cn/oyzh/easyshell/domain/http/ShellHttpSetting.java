package cn.oyzh.easyshell.domain.http;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.common.util.UUIDUtil;
import cn.oyzh.store.jdbc.Column;
import cn.oyzh.store.jdbc.PrimaryKey;
import cn.oyzh.store.jdbc.Table;

import java.io.Serializable;

/**
 * HTTP 工作区设置实体。
 *
 * @author oyzh
 * @since 2026-11-07
 */
@Table("t_http_setting")
public class ShellHttpSetting implements Serializable {

    public static final String VERSION_HTTP_1_1 = "HTTP/1.1";
    public static final String VERSION_HTTP_2 = "HTTP/2";

    /**
     * 数据id
     */
    @Column
    @PrimaryKey
    private String id = UUIDUtil.uuidSimple();

    /**
     * 所属连接id
     */
    @Column
    private String iid;

    /**
     * 默认请求超时秒数
     */
    @Column
    private int timeoutSeconds = 30;

    /**
     * HTTP 版本
     */
    @Column
    private String httpVersion = VERSION_HTTP_1_1;

    /**
     * 是否跟随重定向
     */
    @Column
    private Boolean followRedirects = true;

    /**
     * 是否校验 HTTPS 证书
     */
    @Column
    private Boolean verifySsl = true;

    /**
     * 是否自动美化 JSON
     */
    @Column
    private Boolean prettyJson = true;

    /**
     * 当前环境id
     */
    @Column
    private String activeEnvironmentId;

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

    public int getTimeoutSeconds() {
        return this.timeoutSeconds <= 0 ? 30 : this.timeoutSeconds;
    }

    public void setTimeoutSeconds(int timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
    }

    public String getHttpVersion() {
        if (StringUtil.isBlank(this.httpVersion)
                || "HTTP_1_1".equalsIgnoreCase(this.httpVersion)
                || VERSION_HTTP_1_1.equalsIgnoreCase(this.httpVersion)) {
            return VERSION_HTTP_1_1;
        }
        if ("HTTP_2".equalsIgnoreCase(this.httpVersion)
                || VERSION_HTTP_2.equalsIgnoreCase(this.httpVersion)) {
            return VERSION_HTTP_2;
        }
        return VERSION_HTTP_1_1;
    }

    public void setHttpVersion(String httpVersion) {
        this.httpVersion = httpVersion;
    }

    public boolean isFollowRedirects() {
        return this.followRedirects == null || this.followRedirects;
    }

    public void setFollowRedirects(boolean followRedirects) {
        this.followRedirects = followRedirects;
    }

    public boolean isVerifySsl() {
        return this.verifySsl == null || this.verifySsl;
    }

    public void setVerifySsl(boolean verifySsl) {
        this.verifySsl = verifySsl;
    }

    public boolean isPrettyJson() {
        return this.prettyJson == null || this.prettyJson;
    }

    public void setPrettyJson(boolean prettyJson) {
        this.prettyJson = prettyJson;
    }

    public String getActiveEnvironmentId() {
        return activeEnvironmentId;
    }

    public void setActiveEnvironmentId(String activeEnvironmentId) {
        this.activeEnvironmentId = activeEnvironmentId;
    }

    /**
     * 复制设置，避免客户端持有可变对象。
     *
     * @return 设置副本
     */
    public ShellHttpSetting copy() {
        ShellHttpSetting copy = new ShellHttpSetting();
        copy.id = this.id;
        copy.iid = this.iid;
        copy.timeoutSeconds = this.getTimeoutSeconds();
        copy.httpVersion = this.getHttpVersion();
        copy.followRedirects = this.isFollowRedirects();
        copy.verifySsl = this.isVerifySsl();
        copy.prettyJson = this.isPrettyJson();
        copy.activeEnvironmentId = this.activeEnvironmentId;
        return copy;
    }
}
