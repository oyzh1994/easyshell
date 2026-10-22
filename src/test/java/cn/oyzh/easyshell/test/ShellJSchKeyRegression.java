package cn.oyzh.easyshell.test;

import cn.oyzh.ssh.jsch.JSchKeyUtil;
import cn.oyzh.ssh.domain.SSHProxyConfig;
import cn.oyzh.ssh.util.JschUtil;
import com.jcraft.jsch.Proxy;
import com.jcraft.jsch.ProxyHTTP;
import com.jcraft.jsch.ProxySOCKS4;
import com.jcraft.jsch.ProxySOCKS5;

public final class ShellJSchKeyRegression {

    private ShellJSchKeyRegression() {
    }

    public static void main(String[] args) throws Exception {
        validate(JSchKeyUtil.generateRsa(2048, null), "ssh-rsa", 2048);
        validate(JSchKeyUtil.generateRsa(2048, "jsch-local-passphrase"), "ssh-rsa", 2048, "jsch-local-passphrase");
        validate(JSchKeyUtil.generateDsa(1024, null), "ssh-dss", 1024);
        validate(JSchKeyUtil.generateEcdsa(256, null), "ecdsa-sha2-nistp256", 256);
        validate(JSchKeyUtil.generateEd25519(255, null), "ssh-ed25519", 256);
        validate(JSchKeyUtil.generateEd25519(256, null), "ssh-ed25519", 256);
        validateProxyMapping();

        try {
            JschUtil.initAgentIdentityRepository();
        } catch (Exception ex) {
            String message = String.valueOf(ex.getMessage());
            if (!message.contains("not available")) {
                throw ex;
            }
        }
        System.out.println("JSch key and agent regression passed");
        System.exit(0);
    }

    private static void validate(String[] key, String expectedType, int expectedSize) throws Exception {
        validate(key, expectedType, expectedSize, null);
    }

    private static void validate(String[] key, String expectedType, int expectedSize, String passphrase) throws Exception {
        if (!expectedType.equals(JSchKeyUtil.getKeyType(key[1], passphrase))) {
            throw new IllegalStateException("key type mismatch");
        }
        if (expectedSize != JSchKeyUtil.getKeySize(key[1], passphrase)) {
            throw new IllegalStateException("key size mismatch");
        }
        if (JSchKeyUtil.loadKeysForStr(key[1], passphrase).iterator().next() == null) {
            throw new IllegalStateException("private key was not loaded");
        }
        if (!expectedType.equals(JSchKeyUtil.getKeyType(key[0], null))) {
            throw new IllegalStateException("public key type was not parsed");
        }
        if (expectedSize != JSchKeyUtil.getKeySize(key[0], null)) {
            throw new IllegalStateException("public key size was not parsed");
        }
    }

    private static void validateProxyMapping() {
        assertProxy("http", ProxyHTTP.class);
        assertProxy("socks4", ProxySOCKS4.class);
        assertProxy("socks5", ProxySOCKS5.class);
        assertProxy("socks", ProxySOCKS5.class);
        SSHProxyConfig config = new SSHProxyConfig();
        config.setProtocol("direct");
        if (JschUtil.newProxy(config) != null) {
            throw new IllegalStateException("unknown proxy protocol should be ignored");
        }
    }

    private static void assertProxy(String protocol, Class<? extends Proxy> expectedType) {
        SSHProxyConfig config = new SSHProxyConfig();
        config.setHost("127.0.0.1");
        config.setPort(1080);
        config.setProtocol(protocol);
        Proxy proxy = JschUtil.newProxy(config);
        if (!expectedType.isInstance(proxy)) {
            throw new IllegalStateException("proxy mapping mismatch for protocol: " + protocol);
        }
    }
}
