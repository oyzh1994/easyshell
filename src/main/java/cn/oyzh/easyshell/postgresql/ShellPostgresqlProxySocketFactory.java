package cn.oyzh.easyshell.postgresql;

import cn.oyzh.easyshell.domain.ShellProxyConfig;
import cn.oyzh.easyshell.util.ShellProxyUtil;

import javax.net.SocketFactory;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * PostgreSQL代理连接工厂
 *
 * @author oyzh
 * @since 2026-11-06
 */
public class ShellPostgresqlProxySocketFactory extends SocketFactory {

    /**
     * 代理配置
     */
    private final ShellProxyConfig proxyConfig;

    /**
     * 连接超时
     */
    private final int connectTimeout;

    /**
     * 构造PostgreSQL代理连接工厂
     */
    public ShellPostgresqlProxySocketFactory() {
        this.proxyConfig = null;
        this.connectTimeout = 5000;
    }

    /**
     * 构造PostgreSQL代理连接工厂
     *
     * @param arg 参数，格式为：代理类型|代理地址|代理端口|代理用户|代理密码|连接超时
     */
    public ShellPostgresqlProxySocketFactory(String arg) {
        ShellProxyConfig config = new ShellProxyConfig();
        int timeout = 5000;
        if (arg != null && !arg.isBlank()) {
            String[] arr = arg.split("\\|", -1);
            if (arr.length >= 5) {
                config.setProtocol(this.decode(arr[0]));
                config.setHost(arr[1]);
                config.setPort(this.parseInt(arr[2], 0));
                config.setUser(this.decode(arr[3]));
                config.setPassword(this.decode(arr[4]));
            }
            if (arr.length >= 6) {
                timeout = this.parseInt(arr[5], 5000);
            }
        }
        this.proxyConfig = config;
        this.connectTimeout = timeout;
    }

    /**
     * 解析int
     *
     * @param val          值
     * @param defaultValue 默认值
     * @return 结果
     */
    private int parseInt(String val, int defaultValue) {
        try {
            return Integer.parseInt(val);
        } catch (Exception ex) {
            return defaultValue;
        }
    }

    /**
     * 解码
     *
     * @param val 值
     * @return 结果
     */
    private String decode(String val) {
        if (val == null || val.isEmpty()) {
            return val;
        }
        try {
            return new String(Base64.getUrlDecoder().decode(val), StandardCharsets.UTF_8);
        } catch (Exception ex) {
            return val;
        }
    }

    /**
     * 生成代理参数
     *
     * @param config         代理配置
     * @param connectTimeout 连接超时
     * @return 参数
     */
    public static String buildArg(ShellProxyConfig config, int connectTimeout) {
        if (config == null) {
            return "";
        }
        Base64.Encoder encoder = Base64.getUrlEncoder().withoutPadding();
        String type = config.getProtocol() == null ? "" : encoder.encodeToString(config.getProtocol().getBytes(StandardCharsets.UTF_8));
        String user = config.getUser() == null ? "" : encoder.encodeToString(config.getUser().getBytes(StandardCharsets.UTF_8));
        String password = config.getPassword() == null ? "" : encoder.encodeToString(config.getPassword().getBytes(StandardCharsets.UTF_8));
        return type + "|" + (config.getHost() == null ? "" : config.getHost())
                + "|" + config.getPort()
                + "|" + user + "|" + password + "|" + connectTimeout;
    }

    @Override
    public Socket createSocket(String host, int port) throws IOException {
        return ShellProxyUtil.createSocket(this.proxyConfig, host, port, this.connectTimeout);
    }

    @Override
    public Socket createSocket(String host, int port, InetAddress localHost, int localPort) throws IOException {
        return this.createSocket(host, port);
    }

    @Override
    public Socket createSocket(InetAddress host, int port) throws IOException {
        return this.createSocket(host.getHostAddress(), port);
    }

    @Override
    public Socket createSocket(InetAddress address, int port, InetAddress localAddress, int localPort) throws IOException {
        return this.createSocket(address.getHostAddress(), port);
    }
}
