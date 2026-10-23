package cn.oyzh.easyshell.util;

import cn.oyzh.common.network.ProxyUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.domain.ShellProxyConfig;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

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

//    /**
//     * 初始化代理2
//     *
//     * @param proxyConfig 代理配置
//     * @return 代理对象
//     */
//    public static ProxyConfiguration initProxy2(ShellProxyConfig proxyConfig) {
//        if (proxyConfig == null) {
//            return ProxyConfiguration.builder().build();
//        }
//        String scheme = "";
//        if (proxyConfig.isHttpProxy()) {
//            scheme = "http://";
//        } else if (proxyConfig.isSocksProxy()) {
//            scheme = "socks://";
//        }
//        scheme = scheme + proxyConfig.getHost() + ":" + proxyConfig.getPort();
//        String user = StringUtil.isBlank(proxyConfig.getUser()) ? null : proxyConfig.getUser();
//        String pwd = StringUtil.isBlank(proxyConfig.getPassword()) ? null : proxyConfig.getPassword();
//        return ProxyConfiguration.builder()
//                .endpoint(URI.create(scheme))
//                .username(user)
//                .password(pwd)
//                .build();
//    }

    ///**
    // * 初始化代理3
    // *
    // * @param proxyConfig 代理配置
    // * @return 代理对象
    // */
    //public static ProxyHandler initProxy3(ShellProxyConfig proxyConfig) {
    //    if (proxyConfig == null) {
    //        return null;
    //    }
    //    int port = proxyConfig.getPort();
    //    String host = proxyConfig.getHost();
    //    String user = StringUtil.isBlank(proxyConfig.getUser()) ? null : proxyConfig.getUser();
    //    String pwd = StringUtil.isBlank(proxyConfig.getPassword()) ? null : proxyConfig.getPassword();
    //    InetSocketAddress proxyAddr = new InetSocketAddress(host, port);
    //    ProxyHandler proxyHandler;
    //    if (proxyConfig.isHttpProxy()) {
    //        if (user == null || pwd == null) {
    //            proxyHandler = new HttpProxyHandler(proxyAddr);
    //        } else {
    //            proxyHandler = new HttpProxyHandler(proxyAddr, user, pwd);
    //        }
    //    } else {
    //        if (user == null || pwd == null) {
    //            proxyHandler = new Socks5ProxyHandler(proxyAddr);
    //        } else {
    //            proxyHandler = new Socks5ProxyHandler(proxyAddr, user, pwd);
    //        }
    //    }
    //    return proxyHandler;
    //}

    /**
     * 创建socket
     *
     * @param proxyConfig   代理配置
     * @param targetHost    目标地址
     * @param targetPort    目标端口
     * @param socketTimeout 连接超时
     * @throws IOException 异常
     */
    public static Socket createSocket(ShellProxyConfig proxyConfig, String targetHost, int targetPort, int socketTimeout) throws IOException {
        // 执行代理
        Socket socket = new Socket();
        try {
            socket.setKeepAlive(true);
            socket.setTcpNoDelay(true);
            if (!isNeedProxy(proxyConfig)) {
                socket.connect(new InetSocketAddress(targetHost, targetPort), socketTimeout);
                return socket;
            }
            socket.connect(new InetSocketAddress(proxyConfig.getHost(), proxyConfig.getPort()), socketTimeout);
            if (proxyConfig.isHttpProxy()) {
                httpConnect(socket, proxyConfig, targetHost, targetPort);
            } else if (proxyConfig.isSocksProxy()) {
                ProxyUtil.socks5Handshake(
                        socket,
                        targetHost,
                        targetPort,
                        proxyConfig.isPasswordAuth() ? proxyConfig.getUser() : null,
                        proxyConfig.isPasswordAuth() ? proxyConfig.getPassword() : null
                );
            } else {
                throw new IOException("Unsupported proxy protocol: " + proxyConfig.getProtocol());
            }
            return socket;
        } catch (IOException | RuntimeException ex) {
            try {
                socket.close();
            } catch (IOException ignored) {
            }
            throw ex;
        }
    }

    private static void httpConnect(Socket socket, ShellProxyConfig proxyConfig, String targetHost, int targetPort) throws IOException {
        InputStream input = socket.getInputStream();
        OutputStream output = socket.getOutputStream();
        String authority = formatAuthority(targetHost, targetPort);
        StringBuilder request = new StringBuilder()
                .append("CONNECT ").append(authority).append(" HTTP/1.1\r\n")
                .append("Host: ").append(authority).append("\r\n")
                .append("Proxy-Connection: keep-alive\r\n");
        if (proxyConfig.isPasswordAuth()) {
            String user = StringUtil.isBlank(proxyConfig.getUser()) ? "" : proxyConfig.getUser();
            String password = StringUtil.isBlank(proxyConfig.getPassword()) ? "" : proxyConfig.getPassword();
            String credential = Base64.getEncoder().encodeToString((user + ":" + password).getBytes(StandardCharsets.UTF_8));
            request.append("Proxy-Authorization: Basic ").append(credential).append("\r\n");
        }
        request.append("\r\n");
        output.write(request.toString().getBytes(StandardCharsets.US_ASCII));
        output.flush();

        String statusLine = readAsciiLine(input);
        if (statusLine == null) {
            throw new IOException("HTTP proxy closed before CONNECT response");
        }
        String[] statusParts = statusLine.split("\\s+", 3);
        if (statusParts.length < 2 || !statusParts[0].startsWith("HTTP/")) {
            throw new IOException("Invalid HTTP proxy response: " + statusLine);
        }
        int status;
        try {
            status = Integer.parseInt(statusParts[1]);
        } catch (NumberFormatException ex) {
            throw new IOException("Invalid HTTP proxy status: " + statusLine, ex);
        }
        String header;
        while ((header = readAsciiLine(input)) != null && !header.isEmpty()) {
        }
        if (header == null) {
            throw new IOException("HTTP proxy closed before CONNECT headers ended");
        }
        if (status < 200 || status >= 300) {
            throw new IOException("HTTP proxy CONNECT failed: " + statusLine);
        }
    }

    private static String readAsciiLine(InputStream input) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        int previous = -1;
        while (true) {
            int current = input.read();
            if (current < 0) {
                if (output.size() == 0) {
                    return null;
                }
                break;
            }
            if (previous == '\r' && current == '\n') {
                byte[] bytes = output.toByteArray();
                return new String(bytes, 0, bytes.length - 1, StandardCharsets.US_ASCII);
            }
            output.write(current);
            previous = current;
            if (output.size() > 16 * 1024) {
                throw new IOException("HTTP proxy response line is too long");
            }
        }
        return output.toString(StandardCharsets.US_ASCII);
    }

    private static String formatAuthority(String host, int port) {
        if (host.indexOf(':') >= 0 && !host.startsWith("[")) {
            return "[" + host + "]:" + port;
        }
        return host + ":" + port;
    }

    /**
     * 是否需要代理
     *
     * @param proxyConfig 代理配置
     * @return 结果
     */
    public static boolean isNeedProxy(ShellProxyConfig proxyConfig) {
        if (proxyConfig == null) {
            return false;
        }
        return !proxyConfig.isNoneProxy();
    }
}
