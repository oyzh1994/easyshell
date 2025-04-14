package cn.oyzh.easyshell.shell;

import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.domain.ShellProxyConfig;
import com.jcraft.jsch.Proxy;
import com.jcraft.jsch.ProxyHTTP;


/**
 * @author oyzh
 * @since 2025-03-21
 */
public class ShellClientUtil {

    public static ShellClient newClient(ShellConnect connect) {
        return new ShellClient(connect);
    }

    /**
     * 初始化代理
     *
     * @param proxyConfig 代理配置
     * @return 代理对象
     */
    public static Proxy newProxy(ShellProxyConfig proxyConfig) {
        if (proxyConfig.isHttpProxy()) {
            ProxyHTTP http = new ProxyHTTP(proxyConfig.getHost(), proxyConfig.getPort());
            if (proxyConfig.isPasswordAuth()) {
                http.setUserPasswd(proxyConfig.getUser(), proxyConfig.getPassword());
            }
            return http;
        }
        return null;
    }
}
