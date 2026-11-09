package cn.oyzh.easyshell.http;

import cn.oyzh.common.json.JSONUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.domain.http.ShellHttpEnvironment;
import cn.oyzh.easyshell.domain.http.ShellHttpRequest;
import cn.oyzh.easyshell.domain.http.ShellHttpSetting;
import cn.oyzh.easyshell.internal.ShellBaseClient;
import cn.oyzh.easyshell.file.ShellFileUtil;
import cn.oyzh.easyshell.internal.ShellConnState;
import cn.oyzh.easyshell.util.ShellI18nHelper;
import cn.oyzh.i18n.I18nHelper;
import cn.oyzh.i18n.I18nResourceBundle;
import com.alibaba.fastjson2.JSONObject;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.ProxySelector;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.util.Base64;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.Map;

/**
 * HTTP 工作区客户端。使用 JDK 自带 HttpClient，不依赖 Web 模块。
 *
 * @author oyzh
 * @since 2026-11-07
 */
public class ShellHttpClient implements ShellBaseClient {

    /**
     * 连接
     */
    private final ShellConnect shellConnect;

    /**
     * 连接状态
     */
    private final SimpleObjectProperty<ShellConnState> state =
            new SimpleObjectProperty<>(ShellConnState.NOT_INITIALIZED);

    /**
     * 状态监听
     */
    private final ChangeListener<ShellConnState> stateListener =
            (observable, oldValue, newValue) -> ShellBaseClient.super.onStateChanged(newValue);

    /**
     * Cookie 管理器
     */
    private final CookieManager cookieManager =
            new CookieManager(null, CookiePolicy.ACCEPT_ALL);

    /**
     * 客户端缓存上限
     */
    private static final int MAX_CLIENTS = 8;

    /**
     * 按设置缓存的 HTTP 客户端，避免请求级设置互相影响
     */
    private final Map<String, HttpClientEntry> httpClients =
            new LinkedHashMap<>(16, 0.75f, true);

    /**
     * OAuth 2.0 token 缓存
     */
    private final Map<String, OAuth2Token> oauth2Tokens = new HashMap<>();

    /**
     * 客户端缓存锁
     */
    private final Object clientLock = new Object();

    public ShellHttpClient(ShellConnect shellConnect) {
        this.shellConnect = shellConnect;
        this.addStateListener(this.stateListener);
    }

    @Override
    public void start(int timeout) {
        if (this.isConnected()) {
            return;
        }
        this.setState(ShellConnState.CONNECTING);
        this.setState(ShellConnState.CONNECTED);
    }

    @Override
    public ShellConnect getShellConnect() {
        return shellConnect;
    }

    @Override
    public boolean isConnected() {
        return this.getState() == ShellConnState.CONNECTED;
    }

    @Override
    public ObjectProperty<ShellConnState> stateProperty() {
        return state;
    }

    @Override
    public void close() {
        synchronized (this.clientLock) {
            for (HttpClientEntry entry : this.httpClients.values()) {
                try {
                    entry.client.close();
                } catch (Exception ignored) {
                }
            }
            this.httpClients.clear();
            this.oauth2Tokens.clear();
        }
        this.setState(ShellConnState.CLOSED);
    }

    /**
     * 同步执行一次请求，调用方应放在后台线程。
     *
     * @param definition 请求定义
     * @param workspace  工作区
     * @return 响应
     * @throws Exception 执行异常
     */
    public ShellHttpResponse execute(ShellHttpRequest definition, ShellHttpWorkspace workspace) throws Exception {
        ShellHttpSetting settings = this.effectiveSettings(definition, workspace);
        ClientLease lease = this.acquireClient(settings);
        try {
            return this.execute(definition, workspace, settings, lease.client);
        } finally {
            this.releaseClient(lease);
        }
    }

    /**
     * 使用已租用的客户端执行请求。
     *
     * @param definition 请求定义
     * @param workspace  工作区
     * @param settings   生效设置
     * @param client     HTTP 客户端
     * @return 响应
     * @throws Exception 执行异常
     */
    private ShellHttpResponse execute(ShellHttpRequest definition, ShellHttpWorkspace workspace,
                                      ShellHttpSetting settings, HttpClient client) throws Exception {
        ShellHttpEnvironment environment = workspace.activeEnvironment();
        URI uri = this.prepareUri(definition, environment);
        String method = definition.normalizedMethod();
        HttpRequest.Builder builder = this.requestBuilder(uri, settings, definition, environment);
        HttpRequest.BodyPublisher bodyPublisher = this.bodyPublisher(builder, definition, environment);
        builder.method(method, bodyPublisher);

        long maxBytes = Math.max(1, settings.getMaxResponseSizeMb()) * 1024L * 1024L;
        long begin = System.nanoTime();
        HttpResponse<InputStream> httpResponse = client.send(
                builder.build(), HttpResponse.BodyHandlers.ofInputStream());
        // Digest 认证需要先接收 401 质询，再携带计算后的 Authorization 重试一次
        if (ShellHttpRequest.AUTH_DIGEST.equalsIgnoreCase(definition.getAuthType())
                && httpResponse.statusCode() == 401) {
            String challenge = this.digestChallenge(httpResponse.headers());
            if (challenge != null) {
                try {
                    if (httpResponse.body() != null) {
                        httpResponse.body().close();
                    }
                } catch (Exception ignored) {
                }
                String user = ShellHttpUtil.resolveVariables(definition.getAuthUser(), environment);
                String password = ShellHttpUtil.resolveVariables(definition.getAuthPassword(), environment);
                String authorization = ShellHttpDigest.authorization(
                        user, password, uri, method, challenge);
                builder = this.requestBuilder(uri, settings, definition, environment);
                bodyPublisher = this.bodyPublisher(builder, definition, environment);
                builder.method(method, bodyPublisher);
                builder.setHeader("Authorization", authorization);
                httpResponse = client.send(
                        builder.build(), HttpResponse.BodyHandlers.ofInputStream());
            }
        }
        long durationMs = (System.nanoTime() - begin) / 1_000_000;

        ShellHttpResponse response = new ShellHttpResponse();
        response.setStatusCode(httpResponse.statusCode());
        response.setStatusText(ShellHttpUtil.statusText(httpResponse.statusCode()));
        response.setUri(httpResponse.uri());
        response.setHeaders(new LinkedHashMap<>(httpResponse.headers().map()));
        response.setDurationMs(durationMs);
        String contentType = httpResponse.headers().firstValue("content-type").orElse(null);
        response.setContentType(contentType);
        response.setCharset(ShellHttpUtil.charset(contentType));

        long contentLength = httpResponse.headers().firstValueAsLong("content-length").orElse(-1L);
        boolean head = ShellHttpRequest.METHOD_HEAD.equalsIgnoreCase(method);
        boolean bodyTooLarge = false;
        long bodySize = 0;
        byte[] bytes = new byte[0];
        try (InputStream input = httpResponse.body()) {
            if (input != null && !head) {
                if (contentLength > maxBytes) {
                    bodyTooLarge = true;
                    bodySize = contentLength;
                } else {
                    ByteArrayOutputStream output = new ByteArrayOutputStream();
                    byte[] buffer = new byte[8192];
                    long total = 0;
                    int length;
                    while ((length = input.read(buffer)) != -1) {
                        total += length;
                        if (total > maxBytes) {
                            bodyTooLarge = true;
                            bodySize = Math.max(contentLength, total);
                            break;
                        }
                        output.write(buffer, 0, length);
                    }
                    if (!bodyTooLarge) {
                        bytes = output.toByteArray();
                        bodySize = total;
                    }
                }
            }
        }
        if (!bodyTooLarge) {
            try {
                bytes = ShellHttpUtil.decompress(bytes,
                        httpResponse.headers().firstValue("content-encoding").orElse(null), maxBytes);
                bodySize = bytes.length;
            } catch (ShellHttpUtil.ResponseTooLargeException ex) {
                bodyTooLarge = true;
                bodySize = Math.max(bodySize, maxBytes);
                bytes = null;
            }
        }
        if (bodyTooLarge) {
            response.setBodyTooLarge(true);
            response.setSize(bodySize > 0 ? bodySize : maxBytes);
            if (!ShellHttpUtil.isTextContent(contentType)) {
                response.setBinary(true);
            }
        } else {
            response.setData(bytes);
            response.setSize(bytes.length);
            if (ShellHttpUtil.isTextContent(contentType)) {
                String body = new String(bytes, response.getCharset());
                // 保持原始正文，显示格式由响应体格式选择器决定
                response.setBody(body);
            } else {
                response.setBinary(true);
                if (bytes.length > 0) {
                    try {
                        String extension = ShellHttpUtil.extensionForContentType(contentType);
                        Path file = Path.of(ShellFileUtil.getTempFile(extension));
                        Files.createDirectories(file.toAbsolutePath().getParent());
                        Files.write(file, bytes);
                        response.setTempFile(file.toString());
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }
        return response;
    }

    /**
     * 创建请求构建器。
     *
     * @param uri         请求地址
     * @param settings    生效设置
     * @param definition  请求定义
     * @param environment 当前环境
     * @return 构建器
     */
    private HttpRequest.Builder requestBuilder(URI uri, ShellHttpSetting settings,
                                               ShellHttpRequest definition,
                                               ShellHttpEnvironment environment) throws Exception {
        HttpRequest.Builder builder = HttpRequest.newBuilder(uri)
                .timeout(Duration.ofSeconds(settings.getTimeoutSeconds()));
        this.applyHeaders(builder, definition, environment, settings);
        this.applyAuth(builder, definition, environment);
        if (ShellHttpRequest.AUTH_OAUTH2.equalsIgnoreCase(definition.getAuthType())) {
            this.applyOAuth2(builder, definition, environment, settings);
        }
        return builder;
    }

    /**
     * 应用 OAuth 2.0 Client Credentials 认证。
     *
     * @param builder     请求构建器
     * @param request     请求
     * @param environment 当前环境
     * @param settings    生效设置
     * @throws Exception 获取 token 异常
     */
    private void applyOAuth2(HttpRequest.Builder builder, ShellHttpRequest request,
                             ShellHttpEnvironment environment, ShellHttpSetting settings) throws Exception {
        String token = this.oauth2Token(request, environment, settings);
        if (StringUtil.isBlank(token)) {
            throw new IllegalArgumentException("OAuth 2.0 access token is empty");
        }
        builder.setHeader("Authorization", "Bearer " + token);
    }

    /**
     * 获取 OAuth 2.0 Client Credentials token。
     *
     * @param request     请求
     * @param environment 当前环境
     * @param settings    设置
     * @return token
     * @throws Exception 获取异常
     */
    private String oauth2Token(ShellHttpRequest request, ShellHttpEnvironment environment,
                               ShellHttpSetting settings) throws Exception {
        String tokenUrl = ShellHttpUtil.resolveVariables(request.getOauth2TokenUrl(), environment);
        String clientId = ShellHttpUtil.resolveVariables(request.getOauth2ClientId(), environment);
        String clientSecret = ShellHttpUtil.resolveVariables(request.getOauth2ClientSecret(), environment);
        String scope = ShellHttpUtil.resolveVariables(request.getOauth2Scope(), environment);
        if (StringUtil.isBlank(tokenUrl)) {
            throw new IllegalArgumentException("OAuth 2.0 token URL is empty");
        }
        if (StringUtil.isBlank(clientId) || clientSecret == null) {
            throw new IllegalArgumentException("OAuth 2.0 client credentials are empty");
        }
        String key = tokenUrl + "|" + clientId + "|" + clientSecret + "|" + scope;
        synchronized (this.oauth2Tokens) {
            OAuth2Token cached = this.oauth2Tokens.get(key);
            if (cached != null && cached.expiresAt > System.currentTimeMillis()) {
                return cached.token;
            }
        }
        String form = "grant_type=client_credentials";
        if (StringUtil.isNotBlank(scope)) {
            form += "&scope=" + ShellHttpUtil.encode(scope);
        }
        String basic = Base64.getEncoder().encodeToString(
                (clientId + ":" + clientSecret).getBytes(StandardCharsets.UTF_8));
        URI uri;
        try {
            uri = URI.create(tokenUrl.trim());
        } catch (Exception ex) {
            throw new IllegalArgumentException("OAuth 2.0 token URL is invalid", ex);
        }
        if (uri.getScheme() == null || uri.getHost() == null
                || (!"http".equalsIgnoreCase(uri.getScheme())
                && !"https".equalsIgnoreCase(uri.getScheme()))) {
            throw new IllegalArgumentException("OAuth 2.0 token URL is invalid");
        }
        HttpRequest tokenRequest = HttpRequest.newBuilder(uri)
                .timeout(Duration.ofSeconds(settings.getTimeoutSeconds()))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("Authorization", "Basic " + basic)
                .POST(HttpRequest.BodyPublishers.ofString(form, StandardCharsets.UTF_8))
                .build();
        ClientLease lease = this.acquireClient(settings);
        HttpResponse<String> response;
        try {
            response = lease.client.send(tokenRequest, HttpResponse.BodyHandlers.ofString());
        } finally {
            this.releaseClient(lease);
        }
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IllegalArgumentException("OAuth 2.0 token request failed: "
                    + response.statusCode());
        }
        JSONObject body = JSONUtil.parseObject(response.body());
        String token = body == null ? null : body.getString("access_token");
        if (StringUtil.isBlank(token)) {
            throw new IllegalArgumentException("OAuth 2.0 token response missing access_token");
        }
        long expiresIn = body.getLongValue("expires_in");
        long expiresAt = System.currentTimeMillis()
                + (expiresIn > 0 ? expiresIn * 1000 : 300_000);
        synchronized (this.oauth2Tokens) {
            this.oauth2Tokens.put(key, new OAuth2Token(token, expiresAt));
        }
        return token;
    }

    /**
     * OAuth 2.0 token 缓存项。
     */
    private static class OAuth2Token {
        private final String token;
        private final long expiresAt;

        private OAuth2Token(String token, long expiresAt) {
            this.token = token;
            this.expiresAt = expiresAt;
        }
    }

    /**
     * 获取 Digest 质询值。
     *
     * @param headers 响应头
     * @return 质询值
     */
    private String digestChallenge(HttpHeaders headers) {
        for (String value : headers.allValues("www-authenticate")) {
            if (value != null && value.regionMatches(true, 0, "Digest", 0, 6)) {
                return value;
            }
        }
        return null;
    }

    /**
     * 合并全局设置和请求级设置，请求级设置优先。
     *
     * @param request   请求
     * @param workspace 工作区
     * @return 生效设置
     */
    private ShellHttpSetting effectiveSettings(ShellHttpRequest request, ShellHttpWorkspace workspace) {
        ShellHttpSetting settings = workspace.getSettings().copy();
        if (StringUtil.isNotBlank(request.getHttpVersion())) {
            settings.setHttpVersion(request.getHttpVersion());
        }
        if (request.getFollowRedirects() != null) {
            settings.setFollowRedirects(request.getFollowRedirects());
        }
        if (request.getSendNoCacheHeader() != null) {
            settings.setSendNoCacheHeader(request.getSendNoCacheHeader());
        }
        if (request.getTimeoutSeconds() != null && request.getTimeoutSeconds() > 0) {
            settings.setTimeoutSeconds(request.getTimeoutSeconds());
        }
        return settings;
    }

    /**
     * 准备请求地址。
     *
     * @param request     请求
     * @param environment 环境
     * @return 请求地址
     */
    private URI prepareUri(ShellHttpRequest request, ShellHttpEnvironment environment) {
        String url = request.getUrl();
        if (StringUtil.isBlank(url)) {
            throw new IllegalArgumentException(ShellI18nHelper.httpUrlEmpty());
        }
        url = ShellHttpUtil.resolveVariables(url.trim(), environment);
        URI uri = ShellHttpUtil.resolveUri(url, request.getParams(), environment);
        if (ShellHttpRequest.AUTH_API_KEY.equalsIgnoreCase(request.getAuthType())
                && ShellHttpRequest.PLACEMENT_QUERY.equalsIgnoreCase(request.getAuthKeyPlacement())) {
            String key = ShellHttpUtil.resolveVariables(request.getAuthKeyName(), environment);
            String value = ShellHttpUtil.resolveVariables(request.getAuthKeyValue(), environment);
            if (StringUtil.isNotBlank(key)) {
                String query = ShellHttpUtil.encode(key.trim()) + "=" + ShellHttpUtil.encode(value);
                uri = URI.create(ShellHttpUtil.appendQuery(uri.toString(), query));
            }
        }
        return uri;
    }

    /**
     * 应用请求头。
     *
     * @param builder     请求构建器
     * @param request     请求
     * @param environment 环境
     */
    private void applyHeaders(HttpRequest.Builder builder, ShellHttpRequest request,
                              ShellHttpEnvironment environment, ShellHttpSetting settings) {
        boolean cacheControl = false;
        for (ShellHttpKeyValue row : request.getHeaders()) {
            if (row == null || !row.isEnabled() || StringUtil.isBlank(row.getKey())) {
                continue;
            }
            String name = ShellHttpUtil.resolveVariables(row.getKey(), environment).trim();
            String value = ShellHttpUtil.resolveVariables(row.getValue(), environment);
            if (isSafeHeader(name)) {
                builder.header(name, value == null ? "" : value);
                if ("Cache-Control".equalsIgnoreCase(name)) {
                    cacheControl = true;
                }
            }
        }
        if (settings != null && settings.isSendNoCacheHeader() && !cacheControl) {
            builder.header("Cache-Control", "no-cache");
        }
    }

    /**
     * 应用认证信息。
     *
     * @param builder     请求构建器
     * @param request     请求
     * @param environment 环境
     */
    private void applyAuth(HttpRequest.Builder builder, ShellHttpRequest request,
                           ShellHttpEnvironment environment) {
        String type = StringUtil.blankToDefault(request.getAuthType(), ShellHttpRequest.AUTH_NONE);
        if (ShellHttpRequest.AUTH_DIGEST.equalsIgnoreCase(type)
                || ShellHttpRequest.AUTH_OAUTH2.equalsIgnoreCase(type)) {
            return;
        }
        if (ShellHttpRequest.AUTH_BASIC.equalsIgnoreCase(type)) {
            String user = ShellHttpUtil.resolveVariables(request.getAuthUser(), environment);
            String password = ShellHttpUtil.resolveVariables(request.getAuthPassword(), environment);
            String token = Base64.getEncoder().encodeToString(
                    ((user == null ? "" : user) + ":" + (password == null ? "" : password))
                            .getBytes(StandardCharsets.UTF_8));
            builder.setHeader("Authorization", "Basic " + token);
        } else if (ShellHttpRequest.AUTH_BEARER.equalsIgnoreCase(type)) {
            String token = ShellHttpUtil.resolveVariables(request.getAuthToken(), environment);
            if (StringUtil.isNotBlank(token)) {
                builder.setHeader("Authorization", "Bearer " + token.trim());
            }
        } else if (ShellHttpRequest.AUTH_API_KEY.equalsIgnoreCase(type)
                && ShellHttpRequest.PLACEMENT_HEADER.equalsIgnoreCase(request.getAuthKeyPlacement())) {
            String name = ShellHttpUtil.resolveVariables(request.getAuthKeyName(), environment);
            String value = ShellHttpUtil.resolveVariables(request.getAuthKeyValue(), environment);
            if (StringUtil.isNotBlank(name) && isSafeHeader(name)) {
                builder.setHeader(name.trim(), value == null ? "" : value);
            }
        }
    }

    /**
     * 创建请求体发布器。
     *
     * @param builder     请求构建器
     * @param request     请求
     * @param environment 环境
     * @return 请求体发布器
     */
    private HttpRequest.BodyPublisher bodyPublisher(HttpRequest.Builder builder,
                                                    ShellHttpRequest request,
                                                    ShellHttpEnvironment environment) {
        String type = StringUtil.blankToDefault(request.getBodyType(), ShellHttpRequest.BODY_NONE);
        if (ShellHttpRequest.BODY_NONE.equalsIgnoreCase(type)) {
            return HttpRequest.BodyPublishers.noBody();
        }
        if (ShellHttpRequest.BODY_FORM_DATA.equalsIgnoreCase(type)) {
            String boundary = "----EasyShellBoundary" + UUID.randomUUID().toString().replace("-", "");
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            for (ShellHttpKeyValue row : request.getFormBody()) {
                if (row == null || !row.isEnabled() || StringUtil.isBlank(row.getKey())) {
                    continue;
                }
                String name = escapeMultipart(ShellHttpUtil.resolveVariables(row.getKey(), environment));
                output.writeBytes(("--" + boundary + "\r\n").getBytes(StandardCharsets.UTF_8));
                if (row.isFile()) {
                    String filePath = ShellHttpUtil.resolveVariables(row.getValue(), environment);
                    if (StringUtil.isBlank(filePath)) {
                        throw new IllegalArgumentException(ShellI18nHelper.httpBodyFileRequired());
                    }
                    Path file = Path.of(filePath.trim());
                    if (!Files.isRegularFile(file)) {
                        throw new IllegalArgumentException(ShellI18nHelper.httpBodyFileNotFound(filePath.trim()));
                    }
                    String filename = escapeMultipart(file.getFileName() == null
                            ? file.toString() : file.getFileName().toString());
                    String contentType;
                    try {
                        contentType = Files.probeContentType(file);
                    } catch (Exception ignored) {
                        contentType = null;
                    }
                    if (StringUtil.isBlank(contentType)) {
                        contentType = "application/octet-stream";
                    }
                    output.writeBytes(("Content-Disposition: form-data; name=\"" + name
                            + "\"; filename=\"" + filename + "\"\r\n").getBytes(StandardCharsets.UTF_8));
                    output.writeBytes(("Content-Type: " + contentType + "\r\n\r\n")
                            .getBytes(StandardCharsets.UTF_8));
                    try {
                        output.writeBytes(Files.readAllBytes(file));
                    } catch (IOException ex) {
                        throw new IllegalArgumentException(
                                ShellI18nHelper.httpBodyFileNotFound(filePath.trim()), ex);
                    }
                } else {
                    String value = ShellHttpUtil.resolveVariables(row.getValue(), environment);
                    output.writeBytes(("Content-Disposition: form-data; name=\"" + name + "\"\r\n\r\n")
                            .getBytes(StandardCharsets.UTF_8));
                    output.writeBytes((value == null ? "" : value).getBytes(StandardCharsets.UTF_8));
                }
                output.writeBytes("\r\n".getBytes(StandardCharsets.UTF_8));
            }
            output.writeBytes(("--" + boundary + "--\r\n").getBytes(StandardCharsets.UTF_8));
            if (!containsHeader(request.getHeaders(), "Content-Type")) {
                builder.setHeader("Content-Type", "multipart/form-data; boundary=" + boundary);
            }
            return HttpRequest.BodyPublishers.ofByteArray(output.toByteArray());
        }
        if (ShellHttpRequest.BODY_BINARY.equalsIgnoreCase(type)) {
            String filePath = ShellHttpUtil.resolveVariables(request.getBody(), environment);
            if (StringUtil.isBlank(filePath)) {
                throw new IllegalArgumentException(ShellI18nHelper.httpBodyFileRequired());
            }
            Path file = Path.of(filePath.trim());
            if (!Files.isRegularFile(file)) {
                throw new IllegalArgumentException(ShellI18nHelper.httpBodyFileNotFound(filePath.trim()));
            }
            if (!containsHeader(request.getHeaders(), "Content-Type")) {
                String contentType = null;
                try {
                    contentType = Files.probeContentType(file);
                } catch (Exception ignored) {
                }
                if (StringUtil.isBlank(contentType)) {
                    contentType = "application/octet-stream";
                }
                builder.setHeader("Content-Type", contentType);
            }
            try {
                return HttpRequest.BodyPublishers.ofFile(file);
            } catch (FileNotFoundException ex) {
                throw new IllegalArgumentException(ShellI18nHelper.httpBodyFileNotFound(filePath.trim()), ex);
            }
        }
        String body;
        String contentType;
        if (ShellHttpRequest.BODY_FORM.equalsIgnoreCase(type)) {
            body = ShellHttpUtil.encodeRows(request.getFormBody(), environment);
            contentType = "application/x-www-form-urlencoded";
        } else {
            body = ShellHttpUtil.resolveVariables(request.getBody(), environment);
            contentType = StringUtil.blankToDefault(
                    request.getBodyContentType(), "application/json");
        }
        if (!containsHeader(request.getHeaders(), "Content-Type")) {
            builder.setHeader("Content-Type", contentType);
        }
        return HttpRequest.BodyPublishers.ofString(body == null ? "" : body,
                ShellHttpUtil.charset(contentType));
    }

    /**
     * 转义 multipart 名称
     *
     * @param value 文本
     * @return 转义结果
     */
    private static String escapeMultipart(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\").replace("\"", "\\\"").replace("\r", "").replace("\n", "");
    }

    /**
     * 是否已包含指定请求头。
     *
     * @param rows 请求头
     * @param name 请求头名称
     * @return 结果
     */
    private static boolean containsHeader(List<ShellHttpKeyValue> rows, String name) {
        if (rows == null) {
            return false;
        }
        for (ShellHttpKeyValue row : rows) {
            if (row != null && row.isEnabled() && name.equalsIgnoreCase(row.getKey())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 是否允许用户自定义该请求头。
     *
     * @param name 请求头名称
     * @return 结果
     */
    private static boolean isSafeHeader(String name) {
        if (StringUtil.isBlank(name)) {
            return false;
        }
        String lower = name.toLowerCase(Locale.ROOT);
        return !lower.equals("host")
                && !lower.equals("content-length")
                && !lower.equals("connection")
                && !lower.equals("upgrade");
    }

    /**
     * 获取 HttpClient 版本。
     *
     * @param version 设置中的版本
     * @return 版本
     */
    private static HttpClient.Version httpVersion(String version) {
        return ShellHttpSetting.VERSION_HTTP_2.equalsIgnoreCase(version)
                ? HttpClient.Version.HTTP_2 : HttpClient.Version.HTTP_1_1;
    }

    /**
     * 租用一个 HttpClient，引用计数避免缓存淘汰时关闭正在使用的客户端。
     *
     * @param settings 设置
     * @return 租约
     */
    private ClientLease acquireClient(ShellHttpSetting settings) {
        if (settings == null) {
            settings = new ShellHttpSetting();
        }
        String key = settings.getHttpVersion() + "|" + settings.isFollowRedirects()
                + "|" + settings.isVerifySsl() + "|" + settings.getTimeoutSeconds();
        synchronized (this.clientLock) {
            HttpClientEntry entry = this.httpClients.get(key);
            if (entry == null) {
                entry = new HttpClientEntry(this.createClient(settings));
                this.httpClients.put(key, entry);
            }
            entry.references++;
            this.evictIdleClients();
            return new ClientLease(entry);
        }
    }

    /**
     * 释放客户端租约。
     *
     * @param lease 租约
     */
    private void releaseClient(ClientLease lease) {
        if (lease == null) {
            return;
        }
        synchronized (this.clientLock) {
            lease.entry.references = Math.max(0, lease.entry.references - 1);
            this.evictIdleClients();
        }
    }

    /**
     * 淘汰空闲客户端。
     */
    private void evictIdleClients() {
        while (this.httpClients.size() > MAX_CLIENTS) {
            HttpClientEntry idle = null;
            for (HttpClientEntry entry : this.httpClients.values()) {
                if (entry.references <= 0) {
                    idle = entry;
                    break;
                }
            }
            if (idle == null) {
                return;
            }
            this.httpClients.values().remove(idle);
            try {
                idle.client.close();
            } catch (Exception ignored) {
            }
        }
    }

    /**
     * 客户端租约。
     */
    private static class ClientLease {
        private final HttpClientEntry entry;
        private final HttpClient client;

        private ClientLease(HttpClientEntry entry) {
            this.entry = entry;
            this.client = entry.client;
        }
    }

    /**
     * 客户端缓存项。
     */
    private static class HttpClientEntry {
        private final HttpClient client;
        private int references;

        private HttpClientEntry(HttpClient client) {
            this.client = client;
        }
    }

    /**
     * 创建 HttpClient。
     *
     * @param settings 设置
     * @return 客户端
     */
    private HttpClient createClient(ShellHttpSetting settings) {
        HttpClient.Builder builder = HttpClient.newBuilder()
                .version(httpVersion(settings.getHttpVersion()))
                .connectTimeout(Duration.ofSeconds(settings.getTimeoutSeconds()))
                .followRedirects(settings.isFollowRedirects()
                        ? HttpClient.Redirect.NORMAL : HttpClient.Redirect.NEVER)
                .cookieHandler(this.cookieManager);
        ProxySelector proxySelector = ProxySelector.getDefault();
        if (proxySelector != null) {
            builder.proxy(proxySelector);
        }
        if (!settings.isVerifySsl()) {
            try {
                builder.sslContext(this.trustAllContext());
                SSLParameters parameters = new SSLParameters();
                parameters.setEndpointIdentificationAlgorithm("");
                builder.sslParameters(parameters);
            } catch (Exception ignored) {
            }
        }
        return builder.build();
    }

    /**
     * 创建信任全部证书的 SSL 上下文。
     *
     * @return SSL 上下文
     * @throws Exception 异常
     */
    private static SSLContext trustAllContext() throws Exception {
        TrustManager[] managers = {new X509TrustManager() {
            @Override
            public void checkClientTrusted(X509Certificate[] chain, String authType) {
            }

            @Override
            public void checkServerTrusted(X509Certificate[] chain, String authType) {
            }

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[0];
            }
        }};
        SSLContext context = SSLContext.getInstance("TLS");
        context.init(null, managers, new SecureRandom());
        return context;
    }

    /**
     * 返回响应头的文本形式。
     *
     * @param response 响应
     * @return 文本
     */
    public static String headersText(ShellHttpResponse response) {
        StringBuilder builder = new StringBuilder();
        String urlLabel = I18nResourceBundle.i18nString("base.url");
        String statusLabel = I18nHelper.status();
        if (response.getUri() != null) {
            builder.append(urlLabel).append(": ").append(response.getUri()).append('\n');
        }
        builder.append(statusLabel).append(": ").append(response.getStatusCode());
        if (StringUtil.isNotBlank(response.getStatusText())) {
            builder.append(' ').append(response.getStatusText());
        }
        builder.append("\n\n");
        Map<String, List<String>> headers = response.getHeaders();
        if (headers != null) {
            for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
                for (String value : entry.getValue()) {
                    builder.append(entry.getKey()).append(": ").append(value).append('\n');
                }
            }
        }
        return builder.toString();
    }
}
