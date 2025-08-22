package cn.oyzh.easyshell.util;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.domain.ShellProxyConfig;
import io.netty.handler.proxy.HttpProxyHandler;
import io.netty.handler.proxy.ProxyHandler;
import io.netty.handler.proxy.Socks5ProxyHandler;
import software.amazon.awssdk.http.urlconnection.ProxyConfiguration;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URI;

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
            return Proxy.NO_PROXY;
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

    /**
     * 初始化代理2
     *
     * @param proxyConfig 代理配置
     * @return 代理对象
     */
    public static ProxyConfiguration initProxy2(ShellProxyConfig proxyConfig) {
        if (proxyConfig == null) {
            return ProxyConfiguration.builder().build();
        }
        String scheme = "";
        if (proxyConfig.isHttpProxy()) {
            scheme = "http://";
        } else if (proxyConfig.isSocksProxy()) {
            scheme = "socks://";
        }
        scheme = scheme + proxyConfig.getHost() + ":" + proxyConfig.getPort();
        String user = StringUtil.isBlank(proxyConfig.getUser()) ? null : proxyConfig.getUser();
        String pwd = StringUtil.isBlank(proxyConfig.getPassword()) ? null : proxyConfig.getPassword();
        return ProxyConfiguration.builder()
                .endpoint(URI.create(scheme))
                .username(user)
                .password(pwd)
                .build();
    }

    /**
     * 初始化代理3
     *
     * @param proxyConfig 代理配置
     * @return 代理对象
     */
    public static ProxyHandler initProxy3(ShellProxyConfig proxyConfig) {
        if (proxyConfig == null) {
            return null;
        }
        int port = proxyConfig.getPort();
        String host = proxyConfig.getHost();
        String user = StringUtil.isBlank(proxyConfig.getUser()) ? null : proxyConfig.getUser();
        String pwd = StringUtil.isBlank(proxyConfig.getPassword()) ? null : proxyConfig.getPassword();
        InetSocketAddress proxyAddr = new InetSocketAddress(host, port);
        ProxyHandler proxyHandler;
        if (proxyConfig.isHttpProxy()) {
            if (user == null || pwd == null) {
                proxyHandler = new HttpProxyHandler(proxyAddr);
            } else {
                proxyHandler = new HttpProxyHandler(proxyAddr, user, pwd);
            }
        } else {
            if (user == null || pwd == null) {
                proxyHandler = new Socks5ProxyHandler(proxyAddr);
            } else {
                proxyHandler = new Socks5ProxyHandler(proxyAddr, user, pwd);
            }
        }
        return proxyHandler;
    }
}
