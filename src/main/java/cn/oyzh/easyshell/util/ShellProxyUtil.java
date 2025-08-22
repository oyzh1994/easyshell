package cn.oyzh.easyshell.util;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.easyshell.domain.ShellProxyConfig;
import cn.oyzh.ssh.SSHException;

import java.net.InetSocketAddress;
import java.net.Proxy;

/**
 * @author oyzh
 * @since 2025-08-22
 */
public class ShellProxyUtil {

    /**
     * 初始化代理1
     *
     * @param proxyConfig 代理配置
     * @return 代理对象
     */
    public static Proxy initProxy1(ShellProxyConfig proxyConfig) {
        if (proxyConfig == null) {
            JulLog.warn("proxy is enable but proxy config is null");
            throw new SSHException("proxy is enable but proxy config is null");
        }
        int proxyPort = proxyConfig.getPort();
        String proxyHost = proxyConfig.getHost();
        Proxy proxy = null;
        if (proxyConfig.isHttpProxy()) {
            proxy = new Proxy(
                    Proxy.Type.HTTP,
                    new InetSocketAddress(proxyHost, proxyPort)
            );
        } else if (proxyConfig.isSocksProxy()) {
            proxy = new Proxy(
                    Proxy.Type.SOCKS,
                    new InetSocketAddress(proxyHost, proxyPort)
            );
        }
        return proxy;
    }
}
