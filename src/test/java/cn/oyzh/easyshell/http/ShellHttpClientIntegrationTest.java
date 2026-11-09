package cn.oyzh.easyshell.http;

import cn.oyzh.easyshell.ShellConst;
import cn.oyzh.easyshell.domain.ShellConnect;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import cn.oyzh.easyshell.domain.http.ShellHttpCollection;
import cn.oyzh.easyshell.domain.http.ShellHttpEnvironment;
import cn.oyzh.easyshell.domain.http.ShellHttpRequest;
import cn.oyzh.easyshell.internal.ShellPrototype;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * HTTP 工作区客户端与 mock 服务集成测试。
 *
 * @author oyzh
 * @since 2026-11-07
 */
public class ShellHttpClientIntegrationTest {

    private ShellHttpMockServer server;

    private ShellHttpClient client;

    private ShellHttpWorkspace workspace;

    @Before
    public void setUp() throws Throwable {
        this.server = new ShellHttpMockServer();
        this.server.start();
        ShellConnect connect = new ShellConnect();
        connect.setType(ShellPrototype.HTTP);
        connect.setName("Mock HTTP");
        this.client = new ShellHttpClient(connect);
        this.client.start();
        this.workspace = new ShellHttpWorkspace();
        this.workspace.getSettings().setTimeoutSeconds(10);
    }

    @After
    public void tearDown() {
        if (this.client != null) {
            this.client.close();
        }
        if (this.server != null) {
            this.server.close();
        }
    }

    @Test
    public void sendsGetWithEnvironmentVariablesParamsAndHeaders() throws Exception {
        ShellHttpEnvironment environment = new ShellHttpEnvironment();
        environment.setName("Local");
        environment.getValues().add(new ShellHttpKeyValue("token", "from-env"));
        environment.getValues().add(new ShellHttpKeyValue("q", "中文 value"));
        this.workspace.getEnvironments().add(environment);
        this.workspace.setActiveEnvironmentId(environment.getId());

        ShellHttpRequest request = new ShellHttpRequest();
        request.setMethod("GET");
        request.setUrl("{{baseUrl}}/api/hello");
        environment.getValues().add(new ShellHttpKeyValue("baseUrl", this.server.baseUrl()));
        request.getParams().add(new ShellHttpKeyValue("keyword", "{{q}}"));
        request.getHeaders().add(new ShellHttpKeyValue("X-Test", "{{token}}"));

        ShellHttpResponse response = this.client.execute(request, this.workspace);

        assertEquals(200, response.getStatusCode());
        assertTrue(response.getBody().contains("\"message\":\"hello\""));
        assertTrue(response.getBody().contains("from-env"));
        assertTrue(response.getDurationMs() >= 0);
        assertTrue(response.getSize() > 0);
    }

    @Test
    public void sendsJsonBody() throws Exception {
        ShellHttpRequest request = new ShellHttpRequest();
        request.setMethod("POST");
        request.setUrl(this.server.baseUrl() + "/api/echo");
        request.setBodyType("raw");
        request.setBodyContentType("application/json");
        request.setBody("{\"name\":\"EasyShell\"}");

        ShellHttpResponse response = this.client.execute(request, this.workspace);

        assertEquals(200, response.getStatusCode());
        assertTrue(response.getBody().contains("EasyShell"));
        assertTrue(response.getBody().contains("application/json"));
    }

    @Test
    public void sendsBasicAndBearerAuthentication() throws Exception {
        ShellHttpRequest basic = new ShellHttpRequest();
        basic.setUrl(this.server.baseUrl() + "/auth/basic");
        basic.setAuthType("basic");
        basic.setAuthUser("user");
        basic.setAuthPassword("pass");
        assertEquals(200, this.client.execute(basic, this.workspace).getStatusCode());

        ShellHttpRequest bearer = new ShellHttpRequest();
        bearer.setUrl(this.server.baseUrl() + "/auth/bearer");
        bearer.setAuthType("bearer");
        bearer.setAuthToken("test-token");
        assertEquals(200, this.client.execute(bearer, this.workspace).getStatusCode());
    }

    @Test
    public void sendsApiKeyInHeaderAndQuery() throws Exception {
        ShellHttpRequest header = new ShellHttpRequest();
        header.setUrl(this.server.baseUrl() + "/api/hello");
        header.setAuthType("apiKey");
        header.setAuthKeyName("X-Test");
        header.setAuthKeyValue("header-key");
        header.setAuthKeyPlacement("header");
        assertTrue(this.client.execute(header, this.workspace).getBody().contains("header-key"));

        ShellHttpRequest query = new ShellHttpRequest();
        query.setUrl(this.server.baseUrl() + "/api/hello");
        query.setAuthType("apiKey");
        query.setAuthKeyName("api_key");
        query.setAuthKeyValue("query-key");
        query.setAuthKeyPlacement("query");
        assertTrue(this.client.execute(query, this.workspace).getBody().contains("query-key"));
    }

    @Test
    public void followsRedirectAndReturnsStatus() throws Exception {
        ShellHttpRequest request = new ShellHttpRequest();
        request.setUrl(this.server.baseUrl() + "/redirect");
        request.setMethod("GET");

        ShellHttpResponse response = this.client.execute(request, this.workspace);

        assertEquals(200, response.getStatusCode());
        assertTrue(response.getUri().toString().contains("redirected=true"));
    }

    @Test
    public void createsBrowserPreviewForHtmlWithBaseUri() throws Exception {
        ShellHttpRequest request = new ShellHttpRequest();
        request.setUrl(this.server.baseUrl() + "/html");

        ShellHttpResponse response = this.client.execute(request, this.workspace);
        Path preview = ShellHttpUtil.createHtmlPreview(response.getBody(), response.getUri());
        String html = Files.readString(preview);

        assertTrue(response.isHtml());
        assertTrue(html.contains("<base href=\""));
        assertTrue(html.contains("EasyShell HTTP Preview"));
        Files.deleteIfExists(preview);
    }

    @Test
    public void reportsServerErrorsWithoutThrowing() throws Exception {
        ShellHttpRequest request = new ShellHttpRequest();
        request.setUrl(this.server.baseUrl() + "/error");

        ShellHttpResponse response = this.client.execute(request, this.workspace);

        assertEquals(500, response.getStatusCode());
        assertTrue(response.getBody().contains("mock error"));
    }


    @Test
    public void decodesGzipHtmlResponse() throws Exception {
        ShellHttpRequest request = new ShellHttpRequest();
        request.setUrl(this.server.baseUrl() + "/gzip");

        ShellHttpResponse response = this.client.execute(request, this.workspace);

        assertEquals(200, response.getStatusCode());
        assertFalse(response.isBinary());
        assertTrue(response.getBody().contains("压缩响应"));
        assertTrue(response.getBody().contains("gzip compressed html"));
    }



    @Test
    public void sendsUrlEncodedAndBinaryBodies() throws Exception {
        ShellHttpRequest form = new ShellHttpRequest();
        form.setMethod(ShellHttpRequest.METHOD_POST);
        form.setUrl(this.server.baseUrl() + "/api/echo");
        form.setBodyType(ShellHttpRequest.BODY_FORM);
        form.getFormBody().add(new ShellHttpKeyValue("name", "easy shell"));
        ShellHttpResponse formResponse = this.client.execute(form, this.workspace);
        assertTrue(formResponse.getBody().contains("application/x-www-form-urlencoded"));
        assertTrue(formResponse.getBody().contains("name=easy+shell"));

        Path file = Files.createTempFile("easyshell-http-binary-", ".txt");
        try {
            Files.writeString(file, "binary body", StandardCharsets.UTF_8);
            ShellHttpRequest binary = new ShellHttpRequest();
            binary.setMethod(ShellHttpRequest.METHOD_POST);
            binary.setUrl(this.server.baseUrl() + "/api/echo");
            binary.setBodyType(ShellHttpRequest.BODY_BINARY);
            binary.setBody(file.toString());
            ShellHttpResponse binaryResponse = this.client.execute(binary, this.workspace);
            assertTrue(binaryResponse.getBody().contains("binary body"));
        } finally {
            Files.deleteIfExists(file);
        }
    }


    @Test
    public void sendsMultipartFormDataWithTextAndFile() throws Exception {
        Path file = Files.createTempFile("easyshell-http-form-", ".txt");
        try {
            Files.writeString(file, "form file body", StandardCharsets.UTF_8);
            ShellHttpRequest request = new ShellHttpRequest();
            request.setMethod(ShellHttpRequest.METHOD_POST);
            request.setUrl(this.server.baseUrl() + "/api/echo");
            request.setBodyType(ShellHttpRequest.BODY_FORM_DATA);
            request.getFormBody().add(new ShellHttpKeyValue("text", "hello form"));
            ShellHttpKeyValue fileRow = new ShellHttpKeyValue("upload", file.toString());
            fileRow.setFile(true);
            request.getFormBody().add(fileRow);

            ShellHttpResponse response = this.client.execute(request, this.workspace);

            assertTrue(response.getBody().contains("multipart/form-data"));
            assertTrue(response.getBody().contains("name="));
            assertTrue(response.getBody().contains("text"));
            assertTrue(response.getBody().contains("hello form"));
            assertTrue(response.getBody().contains("filename="));
            assertTrue(response.getBody().contains(file.getFileName().toString()));
            assertTrue(response.getBody().contains("form file body"));
        } finally {
            Files.deleteIfExists(file);
        }
    }


    @Test
    public void savesBinaryDownloadToTempFile() throws Exception {
        ShellHttpRequest request = new ShellHttpRequest();
        request.setUrl(this.server.baseUrl() + "/download");

        ShellHttpResponse response = this.client.execute(request, this.workspace);

        assertTrue(response.isBinary());
        assertNotNull(response.getTempFile());
        assertTrue(response.getTempFile().startsWith(ShellConst.getCachePath()));
        Path file = Path.of(response.getTempFile());
        assertTrue(Files.exists(file));
        assertEquals("EasyShell binary download",
                new String(Files.readAllBytes(file), StandardCharsets.UTF_8));
        Files.deleteIfExists(file);
    }

    @Test
    public void uploadsBinaryFile() throws Exception {
        Path file = Files.createTempFile("easyshell-http-upload-", ".bin");
        try {
            Files.writeString(file, "upload binary body", StandardCharsets.UTF_8);
            ShellHttpRequest request = new ShellHttpRequest();
            request.setMethod(ShellHttpRequest.METHOD_POST);
            request.setUrl(this.server.baseUrl() + "/upload");
            request.setBodyType(ShellHttpRequest.BODY_BINARY);
            request.setBody(file.toString());

            ShellHttpResponse response = this.client.execute(request, this.workspace);

            assertTrue(response.getBody().contains("\"size\""));
            assertTrue(response.getBody().contains(file.toFile().length() + ""));
        } finally {
            Files.deleteIfExists(file);
        }
    }

    @Test
    public void supportsCommonHttpMethods() throws Exception {
        for (String method : List.of("PUT", "PATCH", "DELETE")) {
            ShellHttpRequest request = new ShellHttpRequest();
            request.setMethod(method);
            request.setUrl(this.server.baseUrl() + "/api/method");
            ShellHttpResponse response = this.client.execute(request, this.workspace);
            assertTrue(response.getBody().contains("\"method\":\"" + method + "\""));
        }
    }


    @Test
    public void headResponseHasNoBodyButExposesMethodHeader() throws Exception {
        ShellHttpRequest request = new ShellHttpRequest();
        request.setMethod(ShellHttpRequest.METHOD_HEAD);
        request.setUrl(this.server.baseUrl() + "/api/method");

        ShellHttpResponse response = this.client.execute(request, this.workspace);

        assertEquals(200, response.getStatusCode());
        assertTrue(response.getBody() == null || response.getBody().isEmpty());
        String method = response.getHeaders().entrySet().stream()
                .filter(entry -> "X-Method".equalsIgnoreCase(entry.getKey()))
                .findFirst().map(entry -> entry.getValue().getFirst()).orElse(null);
        assertEquals("HEAD", method);
    }


    @Test
    public void uploadsMixedFormDataWithTextAndFile() throws Exception {
        Path file = Files.createTempFile("easyshell-http-mixed-", ".txt");
        try {
            Files.writeString(file, "mixed file content", StandardCharsets.UTF_8);
            ShellHttpRequest request = new ShellHttpRequest();
            request.setMethod(ShellHttpRequest.METHOD_POST);
            request.setUrl(this.server.baseUrl() + "/upload/form");
            request.setBodyType(ShellHttpRequest.BODY_FORM_DATA);
            request.getFormBody().add(new ShellHttpKeyValue("title", "hello mixed"));
            ShellHttpKeyValue fileRow = new ShellHttpKeyValue("file", file.toString());
            fileRow.setFile(true);
            request.getFormBody().add(fileRow);

            ShellHttpResponse response = this.client.execute(request, this.workspace);

            assertEquals(200, response.getStatusCode());
            JSONObject body = JSONObject.parseObject(response.getBody());
            assertEquals("hello mixed", body.getJSONObject("fields").getString("title"));
            JSONArray files = body.getJSONArray("files");
            assertEquals(1, files.size());
            JSONObject uploaded = files.getJSONObject(0);
            assertEquals("file", uploaded.getString("name"));
            assertEquals(file.getFileName().toString(), uploaded.getString("filename"));
            assertEquals("mixed file content".getBytes(StandardCharsets.UTF_8).length,
                    uploaded.getIntValue("size"));
        } finally {
            Files.deleteIfExists(file);
        }
    }

    @Test
    public void handlesCommonResponseScenarios() throws Exception {
        ShellHttpRequest status = new ShellHttpRequest();
        status.setUrl(this.server.baseUrl() + "/api/status?code=404");
        assertEquals(404, this.client.execute(status, this.workspace).getStatusCode());

        ShellHttpRequest noContent = new ShellHttpRequest();
        noContent.setUrl(this.server.baseUrl() + "/api/no-content");
        assertEquals(204, this.client.execute(noContent, this.workspace).getStatusCode());

        ShellHttpRequest cookie = new ShellHttpRequest();
        cookie.setUrl(this.server.baseUrl() + "/api/cookie");
        ShellHttpResponse cookieResponse = this.client.execute(cookie, this.workspace);
        assertTrue(cookieResponse.getHeaders().entrySet().stream()
                .anyMatch(entry -> "Set-Cookie".equalsIgnoreCase(entry.getKey())));
        List<ShellHttpCookie> cookies = ShellHttpCookie.parse(cookieResponse);
        assertEquals(1, cookies.size());
        assertEquals("session", cookies.getFirst().getName());
        assertEquals("mock", cookies.getFirst().getValue());
        assertEquals("/", cookies.getFirst().getPath());
        assertTrue(cookies.getFirst().isHttpOnly());
        assertEquals("Lax", cookies.getFirst().getSameSite());
        assertTrue(cookies.getFirst().getExpiresText()
                .matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}"));

        ShellHttpRequest apiKey = new ShellHttpRequest();
        apiKey.setUrl(this.server.baseUrl() + "/api/api-key");
        apiKey.getHeaders().add(new ShellHttpKeyValue("X-API-Key", "test-api-key"));
        assertEquals(200, this.client.execute(apiKey, this.workspace).getStatusCode());

        ShellHttpRequest xml = new ShellHttpRequest();
        xml.setUrl(this.server.baseUrl() + "/api/xml");
        ShellHttpResponse xmlResponse = this.client.execute(xml, this.workspace);
        assertTrue(xmlResponse.getContentType().contains("xml"));
        assertTrue(xmlResponse.getBody().contains("<message>hello</message>"));

        ShellHttpRequest gbk = new ShellHttpRequest();
        gbk.setUrl(this.server.baseUrl() + "/api/charset/gbk");
        assertEquals("中文 GBK 响应", this.client.execute(gbk, this.workspace).getBody());
    }


    @Test
    public void doesNotDuplicateQueryParametersAlreadyInUrl() throws Exception {
        ShellHttpRequest request = new ShellHttpRequest();
        request.setUrl(this.server.baseUrl() + "/api/hello?x=1&q=hello%20world");
        request.getParams().add(new ShellHttpKeyValue("x", "1"));
        request.getParams().add(new ShellHttpKeyValue("q", "hello world"));

        ShellHttpResponse response = this.client.execute(request, this.workspace);

        JSONObject body = JSONObject.parseObject(response.getBody());
        String query = body.getString("query");
        assertEquals(2, query.split("&").length);
        assertTrue(query.contains("x=1"));
        assertTrue(query.contains("q=hello"));
    }

    @Test
    public void syncsQueryParametersWithUrlText() {
        List<ShellHttpKeyValue> rows = ShellHttpUtil.parseQuery(
                "https://example.com/api?x=1&q=hello%20world#part");
        assertEquals(2, rows.size());
        assertEquals("x", rows.get(0).getKey());
        assertEquals("hello world", rows.get(1).getValue());
        assertEquals("https://example.com/api#part",
                ShellHttpUtil.removeQuery("https://example.com/api?x=1#part"));
        assertEquals("x=1&q=hello+world",
                ShellHttpUtil.encodeRowsForDisplay(rows));
        assertTrue(ShellHttpUtil.queryMatches(
                "https://example.com/api?x=1&q=hello%20world", "x=1&q=hello+world"));
    }



    @Test
    public void sendsNoCacheHeaderAccordingToSetting() throws Exception {
        ShellHttpRequest request = new ShellHttpRequest();
        request.setUrl(this.server.baseUrl() + "/api/headers");
        boolean old = this.workspace.getSettings().isSendNoCacheHeader();
        try {
            this.workspace.getSettings().setSendNoCacheHeader(true);
            ShellHttpResponse enabled = this.client.execute(request, this.workspace);
            JSONObject enabledHeaders = JSONObject.parseObject(enabled.getBody())
                    .getJSONObject("headers");
            assertTrue(enabledHeaders.entrySet().stream().anyMatch(entry ->
                    "Cache-Control".equalsIgnoreCase(entry.getKey())
                            && String.valueOf(entry.getValue()).contains("no-cache")));

            this.workspace.getSettings().setSendNoCacheHeader(false);
            ShellHttpResponse disabled = this.client.execute(request, this.workspace);
            JSONObject disabledHeaders = JSONObject.parseObject(disabled.getBody())
                    .getJSONObject("headers");
            assertTrue(disabledHeaders.entrySet().stream().noneMatch(entry ->
                    "Cache-Control".equalsIgnoreCase(entry.getKey())));
        } finally {
            this.workspace.getSettings().setSendNoCacheHeader(old);
        }
    }


    @Test
    public void requestSettingsOverrideGlobalNoCacheAndRedirects() throws Exception {
        boolean oldNoCache = this.workspace.getSettings().isSendNoCacheHeader();
        boolean oldRedirect = this.workspace.getSettings().isFollowRedirects();
        try {
            this.workspace.getSettings().setSendNoCacheHeader(false);
            ShellHttpRequest noCache = new ShellHttpRequest();
            noCache.setUrl(this.server.baseUrl() + "/api/headers");
            noCache.setSendNoCacheHeader(true);
            JSONObject headers = JSONObject.parseObject(
                    this.client.execute(noCache, this.workspace).getBody()).getJSONObject("headers");
            assertTrue(headers.entrySet().stream().anyMatch(entry ->
                    "Cache-Control".equalsIgnoreCase(entry.getKey())));

            this.workspace.getSettings().setSendNoCacheHeader(true);
            ShellHttpRequest noCacheDisabled = new ShellHttpRequest();
            noCacheDisabled.setUrl(this.server.baseUrl() + "/api/headers");
            noCacheDisabled.setSendNoCacheHeader(false);
            JSONObject disabledHeaders = JSONObject.parseObject(
                    this.client.execute(noCacheDisabled, this.workspace).getBody())
                    .getJSONObject("headers");
            assertTrue(disabledHeaders.entrySet().stream().noneMatch(entry ->
                    "Cache-Control".equalsIgnoreCase(entry.getKey())));

            this.workspace.getSettings().setFollowRedirects(true);
            ShellHttpRequest noRedirect = new ShellHttpRequest();
            noRedirect.setUrl(this.server.baseUrl() + "/redirect");
            noRedirect.setFollowRedirects(false);
            assertEquals(302, this.client.execute(noRedirect, this.workspace).getStatusCode());

            ShellHttpRequest redirect = new ShellHttpRequest();
            redirect.setUrl(this.server.baseUrl() + "/redirect");
            redirect.setFollowRedirects(true);
            assertEquals(200, this.client.execute(redirect, this.workspace).getStatusCode());
        } finally {
            this.workspace.getSettings().setSendNoCacheHeader(oldNoCache);
            this.workspace.getSettings().setFollowRedirects(oldRedirect);
        }
    }


    @Test
    public void requestTimeoutOverridesGlobalSetting() throws Exception {
        int oldTimeout = this.workspace.getSettings().getTimeoutSeconds();
        try {
            this.workspace.getSettings().setTimeoutSeconds(5);
            ShellHttpRequest timeout = new ShellHttpRequest();
            timeout.setUrl(this.server.baseUrl() + "/api/slow?ms=1500");
            timeout.setTimeoutSeconds(1);
            try {
                this.client.execute(timeout, this.workspace);
                fail("Expected request timeout");
            } catch (Exception expected) {
                // expected
            }

            ShellHttpRequest success = new ShellHttpRequest();
            success.setUrl(this.server.baseUrl() + "/api/slow?ms=1500");
            success.setTimeoutSeconds(3);
            assertEquals(200, this.client.execute(success, this.workspace).getStatusCode());
        } finally {
            this.workspace.getSettings().setTimeoutSeconds(oldTimeout);
        }
    }


    @Test
    public void handlesCssJavaScriptAndMarkdownResponses() throws Exception {
        ShellHttpRequest css = new ShellHttpRequest();
        css.setUrl(this.server.baseUrl() + "/css");
        ShellHttpResponse cssResponse = this.client.execute(css, this.workspace);
        assertTrue(cssResponse.isCss());
        assertTrue(cssResponse.getBody().contains("color: #336699"));
        assertTrue(ShellHttpUtil.isTextContent(cssResponse.getContentType()));

        ShellHttpRequest javascript = new ShellHttpRequest();
        javascript.setUrl(this.server.baseUrl() + "/javascript");
        ShellHttpResponse jsResponse = this.client.execute(javascript, this.workspace);
        assertTrue(jsResponse.isJavaScript());
        assertTrue(jsResponse.getBody().contains("function greet"));
        assertTrue(ShellHttpUtil.isTextContent(jsResponse.getContentType()));

        ShellHttpRequest markdown = new ShellHttpRequest();
        markdown.setUrl(this.server.baseUrl() + "/markdown");
        ShellHttpResponse mdResponse = this.client.execute(markdown, this.workspace);
        assertTrue(mdResponse.isMarkdown());
        assertTrue(mdResponse.getBody().contains("# EasyShell"));
        assertTrue(ShellHttpUtil.isTextContent(mdResponse.getContentType()));
    }


    @Test
    public void formatsCookieExpirationVariants() {
        ShellHttpResponse expires = new ShellHttpResponse();
        expires.setUri(java.net.URI.create("https://example.com/path"));
        expires.setHeaders(Map.of("set-cookie",
                List.of("a=b; Expires=Wed, 9 Jun 2027 10:18:14 GMT")));
        List<ShellHttpCookie> expiresCookies = ShellHttpCookie.parse(expires);
        assertTrue(expiresCookies.getFirst().getExpiresText()
                .matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}"));

        ShellHttpResponse maxAge = new ShellHttpResponse();
        maxAge.setUri(java.net.URI.create("https://example.com/path"));
        maxAge.setHeaders(Map.of("set-cookie",
                List.of("b=c; Max-Age=3600")));
        List<ShellHttpCookie> maxAgeCookies = ShellHttpCookie.parse(maxAge);
        assertTrue(maxAgeCookies.getFirst().getExpiresText()
                .matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}"));
    }


    @Test
    public void rejectsResponseBodyAboveConfiguredLimit() throws Exception {
        int old = this.workspace.getSettings().getMaxResponseSizeMb();
        try {
            this.workspace.getSettings().setMaxResponseSizeMb(1);
            ShellHttpRequest request = new ShellHttpRequest();
            request.setUrl(this.server.baseUrl() + "/api/large?bytes=2097152");
            ShellHttpResponse response = this.client.execute(request, this.workspace);
            assertTrue(response.isBodyTooLarge());
            assertTrue(response.getSize() >= 1024L * 1024L);
            assertTrue(response.getBody() == null || response.getBody().isEmpty());
            assertTrue(response.getTempFile() == null);
        } finally {
            this.workspace.getSettings().setMaxResponseSizeMb(old);
        }
    }


    @Test
    public void decompressesBrotliResponse() throws Exception {
        ShellHttpRequest request = new ShellHttpRequest();
        request.setUrl(this.server.baseUrl() + "/brotli");
        ShellHttpResponse response = this.client.execute(request, this.workspace);
        assertEquals("Brotli compressed response", response.getBody());
    }


    @Test
    public void authenticatesWithDigestChallenge() throws Exception {
        ShellHttpRequest request = new ShellHttpRequest();
        request.setUrl(this.server.baseUrl() + "/auth/digest");
        request.setAuthType(ShellHttpRequest.AUTH_DIGEST);
        request.setAuthUser("user");
        request.setAuthPassword("pass");
        ShellHttpResponse response = this.client.execute(request, this.workspace);
        assertEquals(200, response.getStatusCode());
        assertTrue(response.getBody().contains("\"authenticated\":true"));
    }


    @Test
    public void authenticatesWithOAuth2ClientCredentials() throws Exception {
        ShellHttpRequest request = new ShellHttpRequest();
        request.setUrl(this.server.baseUrl() + "/auth/oauth2");
        request.setAuthType(ShellHttpRequest.AUTH_OAUTH2);
        request.setOauth2TokenUrl(this.server.baseUrl() + "/oauth/token");
        request.setOauth2ClientId("client");
        request.setOauth2ClientSecret("secret");
        request.setOauth2Scope("read");
        ShellHttpResponse response = this.client.execute(request, this.workspace);
        assertEquals(200, response.getStatusCode());
        assertTrue(response.getBody().contains("\"authenticated\":true"));
    }


    @Test
    public void limitsHttpClientCacheSize() throws Exception {
        for (int i = 1; i <= 16; i++) {
            ShellHttpRequest request = new ShellHttpRequest();
            request.setUrl(this.server.baseUrl() + "/api/hello");
            request.setTimeoutSeconds(i);
            assertEquals(200, this.client.execute(request, this.workspace).getStatusCode());
        }
        Field field = ShellHttpClient.class.getDeclaredField("httpClients");
        field.setAccessible(true);
        Map<?, ?> clients = (Map<?, ?>) field.get(this.client);
        assertTrue(clients.size() <= 8);
    }

    @Test
    public void stripsZeroWidthCharactersFromDisplayText() {
        String raw = "下载地址&gt;&gt;\u200D 兰奏云下载地址2&gt;&gt;";
        String text = ShellHttpUtil.stripZeroWidth(raw);
        assertFalse(text.contains("\u200D"));
        assertTrue(text.contains("下载地址&gt;&gt; 兰奏云下载地址2&gt;&gt;"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsRelativeRequestUrlWithoutBaseAddress() throws Exception {
        ShellHttpRequest request = new ShellHttpRequest();
        request.setUrl("/api/hello");
        this.client.execute(request, this.workspace);
    }

}
