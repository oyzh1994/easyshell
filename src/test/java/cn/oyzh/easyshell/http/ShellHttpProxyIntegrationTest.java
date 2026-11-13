package cn.oyzh.easyshell.http;

import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.domain.ShellProxyConfig;
import cn.oyzh.easyshell.domain.http.ShellHttpRequest;
import cn.oyzh.easyshell.internal.ShellPrototype;
import com.alibaba.fastjson2.JSONObject;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.junit.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.concurrent.Executors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * HTTP 客户端代理集成测试。
 *
 * @author oyzh
 * @since 2026-11-13
 */
public class ShellHttpProxyIntegrationTest {

    @Test
    public void usesHttpProxyWithoutAuthentication() throws Throwable {
        try (ProxyServer proxy = new ProxyServer(false, null, null)) {
            assertProxied(proxy, false, null, null);
        }
    }

    @Test
    public void authenticatesHttpProxyWithBasicCredentials() throws Throwable {
        try (ProxyServer proxy = new ProxyServer(true, "proxy-user", "proxy-pass")) {
            assertProxied(proxy, true, "proxy-user", "proxy-pass");
        }
    }

    @Test(expected = IllegalArgumentException.class)
    public void rejectsSocksProxyInsteadOfSilentlyConnectingDirectly() throws Throwable {
        ShellConnect connect = new ShellConnect();
        connect.setType(ShellPrototype.HTTP);
        connect.setName("SOCKS unsupported");
        connect.setEnableProxy(true);
        ShellProxyConfig proxyConfig = new ShellProxyConfig();
        proxyConfig.setProtocol("SOCKS5");
        proxyConfig.setHost("127.0.0.1");
        proxyConfig.setPort(9);
        proxyConfig.setAuthType("none");
        connect.setProxyConfig(proxyConfig);

        ShellHttpClient client = new ShellHttpClient(connect);
        try {
            client.start();
            ShellHttpWorkspace workspace = new ShellHttpWorkspace();
            ShellHttpRequest request = new ShellHttpRequest();
            request.setUrl("http://127.0.0.1/proxy-probe");
            client.execute(request, workspace);
        } finally {
            client.close();
        }
    }

    private static void assertProxied(ProxyServer proxy, boolean auth,
                                      String user, String password) throws Throwable {
        ShellConnect connect = new ShellConnect();
        connect.setType(ShellPrototype.HTTP);
        connect.setName("HTTP proxy");
        connect.setEnableProxy(true);
        connect.setProxyConfig(proxy.config(auth, user, password));

        ShellHttpClient client = new ShellHttpClient(connect);
        try {
            client.start();
            ShellHttpWorkspace workspace = new ShellHttpWorkspace();
            workspace.getSettings().setTimeoutSeconds(5);
            ShellHttpRequest request = new ShellHttpRequest();
            request.setUrl("http://unresolvable.invalid/proxy-probe?source=easyshell");

            ShellHttpResponse response = client.execute(request, workspace);
            assertEquals(200, response.getStatusCode());
            JSONObject body = JSONObject.parseObject(response.getBody());
            assertTrue(body.getBooleanValue("proxied"));
            assertTrue(body.getString("uri").startsWith("http://unresolvable.invalid/proxy-probe"));
            if (auth) {
                assertTrue(body.getBooleanValue("authenticated"));
            }
        } finally {
            client.close();
        }
    }

    /**
     * 最小 HTTP 正向代理。测试请求的域名不可解析，只有经过代理才能成功。
     */
    private static final class ProxyServer implements AutoCloseable {

        private final HttpServer server;

        private final int port;

        private final boolean authentication;

        private final String user;

        private final String password;

        private ProxyServer(boolean authentication, String user, String password) throws IOException {
            this.authentication = authentication;
            this.user = user;
            this.password = password;
            this.server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
            this.port = this.server.getAddress().getPort();
            this.server.setExecutor(Executors.newCachedThreadPool());
            this.server.createContext("/", this::handle);
            this.server.start();
        }

        private ShellProxyConfig config(boolean auth, String user, String password) {
            ShellProxyConfig config = new ShellProxyConfig();
            config.setProtocol("HTTP");
            config.setHost("127.0.0.1");
            config.setPort(this.port);
            config.setAuthType(auth ? "password" : "none");
            config.setUser(user);
            config.setPassword(password);
            return config;
        }

        private void handle(HttpExchange exchange) throws IOException {
            String authorization = exchange.getRequestHeaders().getFirst("Proxy-Authorization");
            String expected = "Basic " + Base64.getEncoder().encodeToString(
                    (this.user + ":" + this.password).getBytes(StandardCharsets.UTF_8));
            boolean authenticated = !this.authentication || expected.equals(authorization);
            if (!authenticated) {
                exchange.getResponseHeaders().set("Proxy-Authenticate", "Basic realm=\"easyshell-proxy\"");
                exchange.sendResponseHeaders(407, -1);
                exchange.close();
                return;
            }
            JSONObject body = new JSONObject();
            body.put("proxied", true);
            body.put("authenticated", authenticated);
            body.put("uri", exchange.getRequestURI().toString());
            byte[] bytes = body.toJSONString().getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
            exchange.sendResponseHeaders(200, bytes.length);
            exchange.getResponseBody().write(bytes);
            exchange.close();
        }

        @Override
        public void close() {
            this.server.stop(0);
        }
    }

}
