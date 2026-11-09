package cn.oyzh.easyshell.domain.http;

import cn.oyzh.common.json.JSONUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.common.util.UUIDUtil;
import cn.oyzh.easyshell.http.ShellHttpKeyValue;
import cn.oyzh.store.jdbc.Column;
import cn.oyzh.store.jdbc.PrimaryKey;
import cn.oyzh.store.jdbc.Table;
import com.alibaba.fastjson2.annotation.JSONField;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * HTTP 请求实体。
 *
 * @author oyzh
 * @since 2026-11-07
 */
@Table("t_http_request")
public class ShellHttpRequest implements Serializable {

    public static final String METHOD_GET = "GET";
    public static final String METHOD_POST = "POST";
    public static final String METHOD_PUT = "PUT";
    public static final String METHOD_PATCH = "PATCH";
    public static final String METHOD_DELETE = "DELETE";
    public static final String METHOD_HEAD = "HEAD";
    public static final String METHOD_OPTIONS = "OPTIONS";
    public static final String BODY_NONE = "none";
    public static final String BODY_RAW = "raw";
    public static final String BODY_FORM = "form";
    public static final String BODY_FORM_DATA = "formData";
    public static final String BODY_BINARY = "binary";
    public static final String AUTH_NONE = "none";
    public static final String AUTH_BASIC = "basic";
    public static final String AUTH_DIGEST = "digest";
    public static final String AUTH_OAUTH2 = "oauth2";
    public static final String AUTH_BEARER = "bearer";
    public static final String AUTH_API_KEY = "apiKey";
    public static final String PLACEMENT_HEADER = "header";
    public static final String PLACEMENT_QUERY = "query";

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
     * 所属集合id
     */
    @Column
    private String collectionId;

    /**
     * 请求名称
     */
    @Column
    private String name = "";

    /**
     * HTTP 方法
     */
    @Column
    private String method = METHOD_GET;

    /**
     * 请求地址
     */
    @Column
    private String url = "";

    /**
     * 查询参数 JSON
     */
    @Column
    private String paramsJson;

    /**
     * 请求头 JSON
     */
    @Column
    private String headersJson;

    /**
     * 请求体类型
     */
    @Column
    private String bodyType = BODY_NONE;

    /**
     * Raw 请求体内容类型
     */
    @Column
    private String bodyContentType = "application/json";

    /**
     * Raw 请求体
     */
    @Column
    private String body = "";

    /**
     * 表单请求体 JSON
     */
    @Column
    private String formBodyJson;

    /**
     * 认证类型
     */
    @Column
    private String authType = AUTH_NONE;

    /**
     * Basic 用户名
     */
    @Column
    private String authUser = "";

    /**
     * Basic 密码
     */
    @Column
    private String authPassword = "";

    /**
     * Bearer Token
     */
    @Column
    private String authToken = "";

    /**
     * API Key 名称
     */
    @Column
    private String authKeyName = "X-API-Key";

    /**
     * API Key
     */
    @Column
    private String authKeyValue = "";

    /**
     * API Key 位置
     */
    @Column
    private String authKeyPlacement = PLACEMENT_HEADER;

    /**
     * OAuth 2.0 Token URL
     */
    @Column
    private String oauth2TokenUrl = "";

    /**
     * OAuth 2.0 Client ID
     */
    @Column
    private String oauth2ClientId = "";

    /**
     * OAuth 2.0 Client Secret
     */
    @Column
    private String oauth2ClientSecret = "";

    /**
     * OAuth 2.0 Scope
     */
    @Column
    private String oauth2Scope = "";

    /**
     * HTTP 版本，空值使用工作区设置
     */
    @Column
    private String httpVersion;

    /**
     * 是否跟随重定向，空值使用工作区设置
     */
    @Column
    private Boolean followRedirects;

    /**
     * 是否发送 no-cache 请求头，空值使用工作区设置
     */
    @Column
    private Boolean sendNoCacheHeader;

    /**
     * 请求超时秒数，空值使用工作区设置
     */
    @Column
    private Integer timeoutSeconds;

    /**
     * 查询参数，运行期数据
     */
    @JSONField(serialize = false, deserialize = false)
    private transient List<ShellHttpKeyValue> params;

    /**
     * 请求头，运行期数据
     */
    @JSONField(serialize = false, deserialize = false)
    private transient List<ShellHttpKeyValue> headers;

    /**
     * 表单请求体，运行期数据
     */
    @JSONField(serialize = false, deserialize = false)
    private transient List<ShellHttpKeyValue> formBody;

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

    public String getCollectionId() {
        return collectionId;
    }

    public void setCollectionId(String collectionId) {
        this.collectionId = collectionId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getParamsJson() {
        return paramsJson;
    }

    public void setParamsJson(String paramsJson) {
        this.paramsJson = paramsJson;
        this.params = null;
    }

    public String getHeadersJson() {
        return headersJson;
    }

    public void setHeadersJson(String headersJson) {
        this.headersJson = headersJson;
        this.headers = null;
    }

    public String getFormBodyJson() {
        return formBodyJson;
    }

    public void setFormBodyJson(String formBodyJson) {
        this.formBodyJson = formBodyJson;
        this.formBody = null;
    }

    public String getBodyType() {
        return bodyType;
    }

    public void setBodyType(String bodyType) {
        this.bodyType = bodyType;
    }

    public String getBodyContentType() {
        return bodyContentType;
    }

    public void setBodyContentType(String bodyContentType) {
        this.bodyContentType = bodyContentType;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getAuthType() {
        return authType;
    }

    public void setAuthType(String authType) {
        this.authType = authType;
    }

    public String getAuthUser() {
        return authUser;
    }

    public void setAuthUser(String authUser) {
        this.authUser = authUser;
    }

    public String getAuthPassword() {
        return authPassword;
    }

    public void setAuthPassword(String authPassword) {
        this.authPassword = authPassword;
    }

    public String getAuthToken() {
        return authToken;
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }

    public String getAuthKeyName() {
        return authKeyName;
    }

    public void setAuthKeyName(String authKeyName) {
        this.authKeyName = authKeyName;
    }

    public String getAuthKeyValue() {
        return authKeyValue;
    }

    public void setAuthKeyValue(String authKeyValue) {
        this.authKeyValue = authKeyValue;
    }

    public String getAuthKeyPlacement() {
        return authKeyPlacement;
    }

    public void setAuthKeyPlacement(String authKeyPlacement) {
        this.authKeyPlacement = authKeyPlacement;
    }

    public String getOauth2TokenUrl() {
        return oauth2TokenUrl;
    }

    public void setOauth2TokenUrl(String oauth2TokenUrl) {
        this.oauth2TokenUrl = oauth2TokenUrl;
    }

    public String getOauth2ClientId() {
        return oauth2ClientId;
    }

    public void setOauth2ClientId(String oauth2ClientId) {
        this.oauth2ClientId = oauth2ClientId;
    }

    public String getOauth2ClientSecret() {
        return oauth2ClientSecret;
    }

    public void setOauth2ClientSecret(String oauth2ClientSecret) {
        this.oauth2ClientSecret = oauth2ClientSecret;
    }

    public String getOauth2Scope() {
        return oauth2Scope;
    }

    public void setOauth2Scope(String oauth2Scope) {
        this.oauth2Scope = oauth2Scope;
    }

    public String getHttpVersion() {
        return httpVersion;
    }

    public void setHttpVersion(String httpVersion) {
        this.httpVersion = httpVersion;
    }

    public Boolean getFollowRedirects() {
        return followRedirects;
    }

    public void setFollowRedirects(Boolean followRedirects) {
        this.followRedirects = followRedirects;
    }

    public Boolean getSendNoCacheHeader() {
        return sendNoCacheHeader;
    }

    public void setSendNoCacheHeader(Boolean sendNoCacheHeader) {
        this.sendNoCacheHeader = sendNoCacheHeader;
    }

    public Integer getTimeoutSeconds() {
        return timeoutSeconds;
    }

    public void setTimeoutSeconds(Integer timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
    }

    @JSONField(serialize = false, deserialize = false)
    public List<ShellHttpKeyValue> getParams() {
        if (this.params == null) {
            this.params = decode(this.paramsJson);
        }
        return this.params;
    }

    public void setParams(List<ShellHttpKeyValue> params) {
        this.params = params;
        this.paramsJson = JSONUtil.toJson(params);
    }

    @JSONField(serialize = false, deserialize = false)
    public List<ShellHttpKeyValue> getHeaders() {
        if (this.headers == null) {
            this.headers = decode(this.headersJson);
        }
        return this.headers;
    }

    public void setHeaders(List<ShellHttpKeyValue> headers) {
        this.headers = headers;
        this.headersJson = JSONUtil.toJson(headers);
    }

    @JSONField(serialize = false, deserialize = false)
    public List<ShellHttpKeyValue> getFormBody() {
        if (this.formBody == null) {
            this.formBody = decode(this.formBodyJson);
        }
        return this.formBody;
    }

    public void setFormBody(List<ShellHttpKeyValue> formBody) {
        this.formBody = formBody;
        this.formBodyJson = JSONUtil.toJson(formBody);
    }

    /**
     * 同步 JSON 字段。
     */
    public void syncJson() {
        this.paramsJson = JSONUtil.toJson(this.getParams());
        this.headersJson = JSONUtil.toJson(this.getHeaders());
        this.formBodyJson = JSONUtil.toJson(this.getFormBody());
    }

    /**
     * 获取规范化的请求方法。
     *
     * @return 请求方法
     */
    @JSONField(serialize = false, deserialize = false)
    public String normalizedMethod() {
        return StringUtil.blankToDefault(this.method, METHOD_GET).trim().toUpperCase(Locale.ROOT);
    }

    /**
     * 复制请求并生成新标识。
     *
     * @return 新请求
     */
    public ShellHttpRequest copy() {
        ShellHttpRequest copy = new ShellHttpRequest();
        copy.iid = this.iid;
        copy.collectionId = this.collectionId;
        copy.name = this.name;
        copy.method = this.method;
        copy.url = this.url;
        copy.params = copyRows(this.getParams());
        copy.headers = copyRows(this.getHeaders());
        copy.bodyType = this.bodyType;
        copy.bodyContentType = this.bodyContentType;
        copy.body = this.body;
        copy.formBody = copyRows(this.getFormBody());
        copy.authType = this.authType;
        copy.authUser = this.authUser;
        copy.authPassword = this.authPassword;
        copy.authToken = this.authToken;
        copy.authKeyName = this.authKeyName;
        copy.authKeyValue = this.authKeyValue;
        copy.authKeyPlacement = this.authKeyPlacement;
        copy.oauth2TokenUrl = this.oauth2TokenUrl;
        copy.oauth2ClientId = this.oauth2ClientId;
        copy.oauth2ClientSecret = this.oauth2ClientSecret;
        copy.oauth2Scope = this.oauth2Scope;
        copy.httpVersion = this.httpVersion;
        copy.followRedirects = this.followRedirects;
        copy.sendNoCacheHeader = this.sendNoCacheHeader;
        copy.timeoutSeconds = this.timeoutSeconds;
        copy.syncJson();
        return copy;
    }

    public static List<ShellHttpKeyValue> copyRows(List<ShellHttpKeyValue> rows) {
        List<ShellHttpKeyValue> copy = new ArrayList<>();
        if (rows != null) {
            for (ShellHttpKeyValue row : rows) {
                if (row != null) {
                    copy.add(row.copy());
                }
            }
        }
        return copy;
    }

    private static List<ShellHttpKeyValue> decode(String json) {
        if (StringUtil.isBlank(json)) {
            return new ArrayList<>();
        }
        try {
            List<ShellHttpKeyValue> list = JSONUtil.toList(json, ShellHttpKeyValue.class);
            return list == null ? new ArrayList<>() : list;
        } catch (Exception ex) {
            return new ArrayList<>();
        }
    }
}
