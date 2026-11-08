package cn.oyzh.easyshell.http;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.domain.http.ShellHttpEnvironment;
import cn.oyzh.easyshell.domain.http.ShellHttpRequest;
import cn.oyzh.easyshell.domain.http.ShellHttpSetting;
import cn.oyzh.easyshell.internal.ShellBaseClient;
import cn.oyzh.easyshell.internal.ShellConnState;
import cn.oyzh.easyshell.util.ShellI18nHelper;
import cn.oyzh.i18n.I18nHelper;
import cn.oyzh.i18n.I18nResourceBundle;
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
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.ProxySelector;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.util.Base64;
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
     * 客户端当前使用的设置快照
     */
    private ShellHttpSetting clientSettings;

    /**
     * HTTP 客户端
     */
    private HttpClient httpClient;

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
        if (this.httpClient != null) {
            try {
                this.httpClient.close();
            } catch (Exception ignored) {
            }
            this.httpClient = null;
        }
        this.clientSettings = null;
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
        this.configure(workspace.getSettings());
        ShellHttpEnvironment environment = workspace.activeEnvironment();
        URI uri = this.prepareUri(definition, environment);
        String method = definition.normalizedMethod();
        HttpRequest.Builder builder = HttpRequest.newBuilder(uri)
                .timeout(Duration.ofSeconds(this.timeoutSeconds(definition, workspace)));
        this.applyHeaders(builder, definition, environment);
        this.applyAuth(builder, definition, environment);
        HttpRequest.BodyPublisher bodyPublisher = this.bodyPublisher(builder, definition, environment);
        builder.method(method, bodyPublisher);

        long begin = System.nanoTime();
        HttpResponse<byte[]> httpResponse = this.httpClient.send(
                builder.build(), HttpResponse.BodyHandlers.ofByteArray());
        long durationMs = (System.nanoTime() - begin) / 1_000_000;

        ShellHttpResponse response = new ShellHttpResponse();
        response.setStatusCode(httpResponse.statusCode());
        response.setStatusText(ShellHttpUtil.statusText(httpResponse.statusCode()));
        response.setUri(httpResponse.uri());
        response.setHeaders(new LinkedHashMap<>(httpResponse.headers().map()));
        response.setDurationMs(durationMs);
        byte[] bytes = httpResponse.body() == null ? new byte[0] : httpResponse.body();
        bytes = ShellHttpUtil.decompress(bytes,
                httpResponse.headers().firstValue("content-encoding").orElse(null));
        response.setSize(bytes.length);
        String contentType = httpResponse.headers().firstValue("content-type").orElse(null);
        response.setContentType(contentType);
        response.setCharset(ShellHttpUtil.charset(contentType));
        if (ShellHttpUtil.isTextContent(contentType)) {
            String body = new String(bytes, response.getCharset());
            // 保持原始正文，显示格式由响应体格式选择器决定
            response.setBody(body);
        } else {
            response.setBinary(true);
            if (bytes.length > 0) {
                try {
                    String extension = ShellHttpUtil.extensionForContentType(contentType);
                    Path file = Files.createTempFile("easyshell-http-", "." + extension);
                    Files.write(file, bytes);
                    response.setTempFile(file.toString());
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
        return response;
    }

    /**
     * 读取请求超时时间。
     *
     * @param request   请求
     * @param workspace 工作区
     * @return 超时秒数
     */
    private int timeoutSeconds(ShellHttpRequest request, ShellHttpWorkspace workspace) {
        if (request.getTimeoutSeconds() != null && request.getTimeoutSeconds() > 0) {
            return request.getTimeoutSeconds();
        }
        return workspace.getSettings().getTimeoutSeconds();
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
                              ShellHttpEnvironment environment) {
        for (ShellHttpKeyValue row : request.getHeaders()) {
            if (row == null || !row.isEnabled() || StringUtil.isBlank(row.getKey())) {
                continue;
            }
            String name = ShellHttpUtil.resolveVariables(row.getKey(), environment).trim();
            String value = ShellHttpUtil.resolveVariables(row.getValue(), environment);
            if (isSafeHeader(name)) {
                builder.header(name, value == null ? "" : value);
            }
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
     * 根据工作区设置重建 HttpClient。
     *
     * @param settings 设置
     */
    private void configure(ShellHttpSetting settings) {
        if (settings == null) {
            settings = new ShellHttpSetting();
        }
        if (this.httpClient != null && this.clientSettings != null
                && this.clientSettings.isFollowRedirects() == settings.isFollowRedirects()
                && this.clientSettings.isVerifySsl() == settings.isVerifySsl()
                && this.clientSettings.getTimeoutSeconds() == settings.getTimeoutSeconds()
                && StringUtil.equals(this.clientSettings.getHttpVersion(), settings.getHttpVersion())) {
            return;
        }
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
        if (this.httpClient != null) {
            try {
                this.httpClient.close();
            } catch (Exception ignored) {
            }
        }
        this.httpClient = builder.build();
        this.clientSettings = settings.copy();
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
