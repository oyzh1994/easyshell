package cn.oyzh.easyshell.http;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.zip.GZIPOutputStream;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 小型 HTTP 测试服务，可通过 main 方法独立启动。
 *
 * @author oyzh
 * @since 2026-11-07
 */
public class ShellHttpMockServer implements AutoCloseable {

    private final HttpServer server;

    private final int port;

    public ShellHttpMockServer() throws IOException {
        this(0);
    }

    public ShellHttpMockServer(int port) throws IOException {
        this.server = HttpServer.create(new InetSocketAddress("127.0.0.1", port), 0);
        this.port = this.server.getAddress().getPort();
        this.server.setExecutor(Executors.newCachedThreadPool());
        this.server.createContext("/api/hello", this::hello);
        this.server.createContext("/api/echo", this::echo);
        this.server.createContext("/api/json", this::jsonData);
        this.server.createContext("/api/headers", this::headers);
        this.server.createContext("/api/method", this::method);
        this.server.createContext("/api/slow", this::slow);
        this.server.createContext("/upload", this::upload);
        this.server.createContext("/upload/form", this::uploadForm);
        this.server.createContext("/download", this::download);
        this.server.createContext("/api/status", this::status);
        this.server.createContext("/api/no-content", this::noContent);
        this.server.createContext("/api/cookie", this::cookie);
        this.server.createContext("/api/api-key", this::apiKey);
        this.server.createContext("/api/xml", this::xml);
        this.server.createContext("/api/charset/gbk", this::gbk);
        this.server.createContext("/auth/basic", this::basicAuth);
        this.server.createContext("/auth/bearer", this::bearer);
        this.server.createContext("/html", this::html);
        this.server.createContext("/gzip", this::gzip);
        this.server.createContext("/redirect", this::redirect);
        this.server.createContext("/error", this::error);
    }

    public void start() {
        this.server.start();
    }

    public String baseUrl() {
        return "http://127.0.0.1:" + this.port;
    }

    public int port() {
        return port;
    }

    @Override
    public void close() {
        this.server.stop(0);
    }

    private void hello(HttpExchange exchange) throws IOException {
        JSONObject body = new JSONObject();
        body.put("message", "hello");
        body.put("method", exchange.getRequestMethod());
        body.put("query", exchange.getRequestURI().getRawQuery());
        body.put("xTest", first(exchange.getRequestHeaders(), "X-Test"));
        this.json(exchange, 200, body);
    }

    private void echo(HttpExchange exchange) throws IOException {
        String requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
        JSONObject body = new JSONObject();
        body.put("method", exchange.getRequestMethod());
        body.put("contentType", first(exchange.getRequestHeaders(), "Content-Type"));
        body.put("body", requestBody);
        this.json(exchange, 200, body);
    }

    private void jsonData(HttpExchange exchange) throws IOException {
        JSONObject data = new JSONObject();
        data.put("id", 1);
        data.put("name", "EasyShell");
        JSONObject body = new JSONObject();
        body.put("code", 0);
        body.put("message", "ok");
        body.put("data", data);
        this.json(exchange, 200, body);
    }

    private void uploadForm(HttpExchange exchange) throws IOException {
        String contentType = first(exchange.getRequestHeaders(), "Content-Type");
        if (contentType == null || !contentType.toLowerCase().contains("multipart/form-data")) {
            this.json(exchange, 400, Map.of("message", "multipart/form-data required"));
            return;
        }
        String boundary = null;
        for (String part : contentType.split(";")) {
            String value = part.trim();
            if (value.startsWith("boundary=")) {
                boundary = value.substring("boundary=".length()).replace("\"", "");
                break;
            }
        }
        if (boundary == null || boundary.isBlank()) {
            this.json(exchange, 400, Map.of("message", "boundary required"));
            return;
        }
        String raw = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.ISO_8859_1);
        JSONObject fields = new JSONObject();
        JSONArray files = new JSONArray();
        Pattern namePattern = Pattern.compile("name=\"([^\"]*)\"");
        Pattern fileNamePattern = Pattern.compile("filename=\"([^\"]*)\"");
        for (String part : raw.split(Pattern.quote("--" + boundary))) {
            if (part.isBlank() || part.startsWith("--")) {
                continue;
            }
            while (part.startsWith("\r\n")) {
                part = part.substring(2);
            }
            int index = part.indexOf("\r\n\r\n");
            if (index < 0) {
                continue;
            }
            String headers = part.substring(0, index);
            String content = part.substring(index + 4);
            if (content.endsWith("\r\n")) {
                content = content.substring(0, content.length() - 2);
            }
            Matcher nameMatcher = namePattern.matcher(headers);
            if (!nameMatcher.find()) {
                continue;
            }
            String name = nameMatcher.group(1);
            Matcher fileMatcher = fileNamePattern.matcher(headers);
            if (fileMatcher.find()) {
                JSONObject file = new JSONObject();
                file.put("name", name);
                file.put("filename", fileMatcher.group(1));
                file.put("size", content.getBytes(StandardCharsets.ISO_8859_1).length);
                String fileType = null;
                for (String line : headers.split("\r\n")) {
                    if (line.toLowerCase().startsWith("content-type:")) {
                        fileType = line.substring("content-type:".length()).trim();
                    }
                }
                file.put("contentType", fileType);
                files.add(file);
            } else {
                fields.put(name, new String(content.getBytes(StandardCharsets.ISO_8859_1),
                        StandardCharsets.UTF_8));
            }
        }
        JSONObject body = new JSONObject();
        body.put("fields", fields);
        body.put("files", files);
        this.json(exchange, 200, body);
    }

    private void status(HttpExchange exchange) throws IOException {
        int code = 200;
        String query = exchange.getRequestURI().getQuery();
        if (query != null) {
            for (String part : query.split("&")) {
                if (part.startsWith("code=")) {
                    try {
                        code = Integer.parseInt(part.substring(5));
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
        }
        this.json(exchange, code, Map.of("status", code));
    }

    private void noContent(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().set("X-Method", exchange.getRequestMethod());
        exchange.sendResponseHeaders(204, -1);
        exchange.close();
    }

    private void cookie(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().add("Set-Cookie", "session=mock; Path=/; HttpOnly");
        this.json(exchange, 200, Map.of("cookie", "session=mock"));
    }

    private void apiKey(HttpExchange exchange) throws IOException {
        boolean accepted = "test-api-key".equals(first(exchange.getRequestHeaders(), "X-API-Key"));
        this.json(exchange, accepted ? 200 : 401,
                Map.of("authenticated", accepted, "scheme", "apiKey"));
    }

    private void xml(HttpExchange exchange) throws IOException {
        this.text(exchange, 200, "application/xml; charset=UTF-8",
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?><root><message>hello</message></root>");
    }

    private void gbk(HttpExchange exchange) throws IOException {
        byte[] bytes = "中文 GBK 响应".getBytes(Charset.forName("GBK"));
        exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=GBK");
        exchange.getResponseHeaders().set("X-Method", exchange.getRequestMethod());
        exchange.sendResponseHeaders(200, bytes.length);
        exchange.getResponseBody().write(bytes);
        exchange.close();
    }

    private void method(HttpExchange exchange) throws IOException {
        JSONObject body = new JSONObject();
        body.put("method", exchange.getRequestMethod());
        body.put("query", exchange.getRequestURI().getRawQuery());
        body.put("bodySize", exchange.getRequestBody().readAllBytes().length);
        this.json(exchange, 200, body);
    }

    private void upload(HttpExchange exchange) throws IOException {
        byte[] bytes = exchange.getRequestBody().readAllBytes();
        JSONObject body = new JSONObject();
        body.put("size", bytes.length);
        body.put("contentType", first(exchange.getRequestHeaders(), "Content-Type"));
        this.json(exchange, 200, body);
    }

    private void download(HttpExchange exchange) throws IOException {
        byte[] bytes = "EasyShell binary download".getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/octet-stream");
        exchange.getResponseHeaders().set("Content-Disposition", "attachment; filename=\"easyshell.bin\"");
        exchange.sendResponseHeaders(200, bytes.length);
        exchange.getResponseBody().write(bytes);
        exchange.close();
    }

    private void headers(HttpExchange exchange) throws IOException {
        JSONObject headers = new JSONObject();
        exchange.getRequestHeaders().forEach((key, values) ->
                headers.put(key, values.size() == 1 ? values.getFirst() : values));
        this.json(exchange, 200, Map.of("headers", headers));
    }

    private void slow(HttpExchange exchange) throws IOException {
        try {
            Thread.sleep(1500);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
        this.json(exchange, 200, Map.of("message", "slow response"));
    }

    /**
     * 用户名 user
     * 密码 pass
     * @param exchange
     * @throws IOException
     */
    private void basicAuth(HttpExchange exchange) throws IOException {
        boolean accepted = "Basic dXNlcjpwYXNz".equals(first(exchange.getRequestHeaders(), "Authorization"));
        this.json(exchange, accepted ? 200 : 401,
                Map.of("authenticated", accepted, "scheme", "basic"));
    }

    private void bearer(HttpExchange exchange) throws IOException {
        boolean accepted = "Bearer test-token".equals(first(exchange.getRequestHeaders(), "Authorization"));
        this.json(exchange, accepted ? 200 : 401,
                Map.of("authenticated", accepted, "scheme", "bearer"));
    }

    private void html(HttpExchange exchange) throws IOException {
        String html = "<!doctype html><html><head><title>Mock</title></head>"
                + "<body><h1>EasyShell HTTP Preview</h1><a href=\"/api/hello\">Hello</a></body></html>";
        this.text(exchange, 200, "text/html; charset=UTF-8", html);
    }

    private void gzip(HttpExchange exchange) throws IOException {
        String html = "<!doctype html><html><head><meta charset=\"UTF-8\"><title>Gzip</title></head>"
                + "<body><h1>压缩响应</h1><p>gzip compressed html</p></body></html>";
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (GZIPOutputStream gzip = new GZIPOutputStream(output)) {
            gzip.write(html.getBytes(StandardCharsets.UTF_8));
        }
        byte[] bytes = output.toByteArray();
        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
        exchange.getResponseHeaders().set("Content-Encoding", "gzip");
        exchange.sendResponseHeaders(200, bytes.length);
        exchange.getResponseBody().write(bytes);
        exchange.close();
    }

    private void redirect(HttpExchange exchange) throws IOException {
        exchange.getResponseHeaders().add("Location", "/api/hello?redirected=true");
        exchange.sendResponseHeaders(302, -1);
        exchange.close();
    }

    private void error(HttpExchange exchange) throws IOException {
        this.json(exchange, 500, Map.of("message", "mock error"));
    }

    private void json(HttpExchange exchange, int status, Object body) throws IOException {
        this.text(exchange, status, "application/json; charset=UTF-8", JSONObject.toJSONString(body));
    }

    private void text(HttpExchange exchange, int status, String contentType, String body)
            throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", contentType);
        // HEAD 响应不允许有响应体，通过响应头返回请求方法便于验证
        exchange.getResponseHeaders().set("X-Method", exchange.getRequestMethod());
        boolean head = "HEAD".equalsIgnoreCase(exchange.getRequestMethod());
        exchange.sendResponseHeaders(status, head ? -1 : bytes.length);
        if (!head) {
            exchange.getResponseBody().write(bytes);
        }
        exchange.close();
    }

    private static String first(Headers headers, String name) {
        List<String> values = headers.get(name);
        return values == null || values.isEmpty() ? null : values.getFirst();
    }

    public static void main(String[] args) throws Exception {
        ShellHttpMockServer server = new ShellHttpMockServer(18081);
        server.start();
        System.out.println("Mock HTTP server: " + server.baseUrl());
        System.out.println("Endpoints: /api/hello /api/echo /api/json /api/headers /api/method /api/slow "
                + "/api/status /api/no-content /api/cookie /api/api-key /api/xml /api/charset/gbk "
                + "/upload /upload/form /download /auth/basic /auth/bearer /html /gzip /redirect /error");
        Runtime.getRuntime().addShutdownHook(new Thread(server::close));
    }
}
