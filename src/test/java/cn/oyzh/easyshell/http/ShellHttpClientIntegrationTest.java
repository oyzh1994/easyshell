package cn.oyzh.easyshell.http;

import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.domain.http.ShellHttpCollection;
import cn.oyzh.easyshell.domain.http.ShellHttpEnvironment;
import cn.oyzh.easyshell.domain.http.ShellHttpRequest;
import cn.oyzh.easyshell.internal.ShellPrototype;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;
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
