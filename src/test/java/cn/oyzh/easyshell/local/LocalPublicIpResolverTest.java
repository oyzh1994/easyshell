package cn.oyzh.easyshell.local;

import com.sun.net.httpserver.HttpServer;
import org.junit.Test;

import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class LocalPublicIpResolverTest {

    @Test
    public void fallsBackUntilAPublicIpIsFound() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/private", exchange -> {
            byte[] body = "192.168.1.10".getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        server.createContext("/public", exchange -> {
            byte[] body = "当前 IP：8.8.8.8 来自于：测试".getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        server.start();
        try {
            String base = "http://127.0.0.1:" + server.getAddress().getPort();
            LocalPublicIpResolver resolver = new LocalPublicIpResolver(
                    List.of(URI.create(base + "/private"), URI.create(base + "/public")),
                    Duration.ofSeconds(2),
                    ProxySelector.of(null));

            LocalPublicIpResolver.Resolution resolution = resolver.resolve();

            assertEquals("8.8.8.8", resolution.ip());
            assertTrue(resolution.source().endsWith("/public"));
            assertEquals(1, resolution.errors().size());
        } finally {
            server.stop(0);
        }
    }

    @Test
    public void extractsIpFromPlainAndLocalizedResponses() {
        assertEquals("8.8.8.8", LocalPublicIpResolver.extractPublicIp("8.8.8.8\n"));
        assertEquals("1.1.1.1", LocalPublicIpResolver.extractPublicIp(
                "当前 IP：1.1.1.1 来自于：Cloudflare"));
        assertNull(LocalPublicIpResolver.extractPublicIp("<html>not an ip</html>"));
    }

    @Test
    public void rejectsPrivateReservedAndDocumentationAddresses() {
        assertFalse(LocalPublicIpResolver.isPublicIp("127.0.0.1"));
        assertFalse(LocalPublicIpResolver.isPublicIp("10.0.0.1"));
        assertFalse(LocalPublicIpResolver.isPublicIp("172.16.0.1"));
        assertFalse(LocalPublicIpResolver.isPublicIp("192.168.1.1"));
        assertFalse(LocalPublicIpResolver.isPublicIp("100.64.0.1"));
        assertFalse(LocalPublicIpResolver.isPublicIp("169.254.1.1"));
        assertFalse(LocalPublicIpResolver.isPublicIp("192.0.2.1"));
        assertFalse(LocalPublicIpResolver.isPublicIp("198.51.100.1"));
        assertFalse(LocalPublicIpResolver.isPublicIp("203.0.113.1"));
        assertFalse(LocalPublicIpResolver.isPublicIp("fc00::1"));
        assertFalse(LocalPublicIpResolver.isPublicIp("2001:db8::1"));
        assertTrue(LocalPublicIpResolver.isPublicIp("8.8.8.8"));
        assertTrue(LocalPublicIpResolver.isPublicIp("2001:4860:4860::8888"));
    }
}
