package cn.oyzh.easyshell.mariadb;

import cn.oyzh.easyshell.domain.ShellProxyConfig;
import cn.oyzh.easyshell.util.ShellProxyUtil;
import org.mariadb.jdbc.Configuration;
import org.mariadb.jdbc.HostAddress;
import org.mariadb.jdbc.util.ConfigurableSocketFactory;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.List;
import java.util.Properties;

/**
 * MariaDB代理连接工厂
 *
 * @author oyzh
 * @since 2026-10-09
 */
public class ShellMariadbProxySocketFactory extends ConfigurableSocketFactory {

    /**
     * 驱动配置
     */
    private Configuration configuration;

    /**
     * 目标主机
     */
    private String targetHost;

    @Override
    public void setConfiguration(Configuration configuration, String host) {
        this.configuration = configuration;
        this.targetHost = host;
    }

    @Override
    public Socket createSocket() throws IOException {
        HostAddress target = this.targetAddress();
        return this.createProxySocket(target.host, target.port);
    }

    @Override
    public Socket createSocket(String host, int port) throws IOException {
        return this.createProxySocket(host, port);
    }

    @Override
    public Socket createSocket(InetAddress host, int port) throws IOException {
        return this.createProxySocket(host.getHostAddress(), port);
    }

    @Override
    public Socket createSocket(String host, int port, InetAddress localHost, int localPort) throws IOException {
        return this.createProxySocket(host, port);
    }

    @Override
    public Socket createSocket(InetAddress address, int port, InetAddress localAddress, int localPort) throws IOException {
        return this.createProxySocket(address.getHostAddress(), port);
    }

    /**
     * 创建已连接的代理套接字
     *
     * @param host 目标主机
     * @param port 目标端口
     * @return 套接字
     * @throws IOException 连接异常
     */
    private Socket createProxySocket(String host, int port) throws IOException {
        try {
            Properties properties = this.configuration.nonMappedOptions();
            ShellProxyConfig proxyConfig = new ShellProxyConfig();
            proxyConfig.setHost(properties.getProperty("_proxyHost"));
            proxyConfig.setUser(properties.getProperty("_proxyUser"));
            proxyConfig.setProtocol(properties.getProperty("_proxyType"));
            proxyConfig.setPassword(properties.getProperty("_proxyPassword"));
            proxyConfig.setPort(Integer.parseInt(properties.getProperty("_proxyPort")));
            return ShellProxyUtil.createSocket(proxyConfig, host, port, this.configuration.connectTimeout());
        } catch (Exception ex) {
            throw new IOException(ex);
        }
    }

    /**
     * 获取目标地址
     *
     * @return 目标地址
     */
    private HostAddress targetAddress() {
        List<HostAddress> addresses = this.configuration.addresses();
        if (addresses == null || addresses.isEmpty()) {
            throw new IllegalStateException("MariaDB target address is not configured");
        }
        for (HostAddress address : addresses) {
            if (address.host.equals(this.targetHost)) {
                return address;
            }
        }
        return addresses.get(0);
    }
}
