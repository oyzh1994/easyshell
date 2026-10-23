package cn.oyzh.easyshell.test.rdp;

import cn.oyzh.easyshell.domain.ShellProxyConfig;
import cn.oyzh.easyshell.rdp.RDPProxyTunnel;
import org.junit.Assume;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class RDPProxyDockerIntegrationTest {

    private static final String HOST = System.getProperty("rdp.proxy.host", "192.168.22.157");
    private static final int PORT = Integer.getInteger("rdp.proxy.port", 3389);

    @Test
    public void dockerHttpOpen() throws Exception {
        Assume.assumeTrue("enable with -Drdp.proxy.it=true", Boolean.getBoolean("rdp.proxy.it"));
        RDPProxyTunnel.verifyConnectivity(config("HTTP", 31280, false, null, null), HOST, PORT, 5000);
    }

    @Test
    public void dockerHttpAuthentication() throws Exception {
        Assume.assumeTrue("enable with -Drdp.proxy.it=true", Boolean.getBoolean("rdp.proxy.it"));
        RDPProxyTunnel.verifyConnectivity(config("HTTP", 31281, true, "proxyuser", "proxy-pass"), HOST, PORT, 5000);
    }

    @Test
    public void dockerSocksOpen() throws Exception {
        Assume.assumeTrue("enable with -Drdp.proxy.it=true", Boolean.getBoolean("rdp.proxy.it"));
        RDPProxyTunnel.verifyConnectivity(config("SOCKS5", 31282, false, null, null), HOST, PORT, 5000);
    }

    @Test
    public void dockerSocksAuthentication() throws Exception {
        Assume.assumeTrue("enable with -Drdp.proxy.it=true", Boolean.getBoolean("rdp.proxy.it"));
        RDPProxyTunnel.verifyConnectivity(config("SOCKS5", 31283, true, "proxyuser", "proxy-pass"), HOST, PORT, 5000);
    }

    @Test
    public void dockerAuthenticationFailures() {
        Assume.assumeTrue("enable with -Drdp.proxy.it=true", Boolean.getBoolean("rdp.proxy.it"));
        assertRejected(config("HTTP", 31281, true, "proxyuser", "wrong"), HOST, PORT);
        assertRejected(config("SOCKS5", 31283, true, "proxyuser", "wrong"), HOST, PORT);
    }

    @Test
    public void benchmarkRdpProxyConnectivity() {
        Assume.assumeTrue("enable with -Drdp.proxy.it=true", Boolean.getBoolean("rdp.proxy.it"));
        List<String> scenarios = new ArrayList<>();
        scenarios.add("direct");
        scenarios.add("http-open");
        scenarios.add("http-auth");
        scenarios.add("socks-open");
        scenarios.add("socks-auth");
        for (String scenario : scenarios) {
            for (int i = 0; i < 3; i++) {
                runScenario(scenario);
            }
            List<Long> samples = new ArrayList<>();
            for (int i = 0; i < 20; i++) {
                long started = System.nanoTime();
                runScenario(scenario);
                samples.add((System.nanoTime() - started) / 1_000_000);
            }
            Collections.sort(samples);
            long min = samples.getFirst();
            long max = samples.getLast();
            double average = samples.stream().mapToLong(Long::longValue).average().orElse(0);
            long p95 = samples.get(Math.min(samples.size() - 1, (int) Math.ceil(samples.size() * 0.95) - 1));
            System.out.printf("BENCH %-12s min=%d ms avg=%.1f ms p95=%d ms max=%d ms%n", scenario, min, average, p95, max);
        }
        assertTrue(true);
    }

    private static void runScenario(String scenario) {
        try {
            switch (scenario) {
                case "direct" -> RDPProxyTunnel.verifyConnectivity(null, HOST, PORT, 5000);
                case "http-open" -> RDPProxyTunnel.verifyConnectivity(config("HTTP", 31280, false, null, null), HOST, PORT, 5000);
                case "http-auth" -> RDPProxyTunnel.verifyConnectivity(config("HTTP", 31281, true, "proxyuser", "proxy-pass"), HOST, PORT, 5000);
                case "socks-open" -> RDPProxyTunnel.verifyConnectivity(config("SOCKS5", 31282, false, null, null), HOST, PORT, 5000);
                case "socks-auth" -> RDPProxyTunnel.verifyConnectivity(config("SOCKS5", 31283, true, "proxyuser", "proxy-pass"), HOST, PORT, 5000);
                default -> throw new IllegalArgumentException(scenario);
            }
        } catch (IOException ex) {
            throw new IllegalStateException(ex);
        }
    }

    private static void assertRejected(ShellProxyConfig config, String host, int port) {
        try {
            RDPProxyTunnel.verifyConnectivity(config, host, port, 5000);
            fail("authentication should fail");
        } catch (IOException expected) {
        }
    }

    private static ShellProxyConfig config(String protocol, int port, boolean auth, String user, String password) {
        ShellProxyConfig config = new ShellProxyConfig();
        config.setHost("127.0.0.1");
        config.setPort(port);
        config.setProtocol(protocol);
        config.setAuthType(auth ? "password" : "none");
        config.setUser(user);
        config.setPassword(password);
        return config;
    }
}
