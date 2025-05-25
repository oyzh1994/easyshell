package cn.oyzh.easyshell.ssh;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.domain.ShellProxyConfig;
import cn.oyzh.easyshell.file.ShellFileUtil;
import cn.oyzh.ssh.util.SSHUtil;
import com.jcraft.jsch.Proxy;
import com.jcraft.jsch.ProxyHTTP;
import com.jcraft.jsch.ProxySOCKS4;
import com.jcraft.jsch.ProxySOCKS5;


/**
 * @author oyzh
 * @since 2025-03-21
 */
public class ShellSSHUtil {

    /**
     * 解析工作目录
     *
     * @param output 输出
     * @return 工作目录
     */
    public static String resolveWorkerDir(String output) {
        if (StringUtil.isBlank(output) || !output.contains("@")) {
            return null;
        }
        // 获取最后一行
        String line = output.lines().toList().getLast();
        // 移除ansi字符串
        line = SSHUtil.removeAnsi(line);
        // 针对部分情况下返回的ansi字符做处理
        if (line.endsWith("?25h")) {
            line = line.substring(0, line.length() - 6);
        }
        // 目录
        String dir = null;
        // linux、unix、macos
        if (StringUtil.endWithAny(line, "# ", "@ ")) {
            line = line.substring(0, line.length() - 2);
            line = line.substring(line.lastIndexOf("@"));
            dir = line.substring(line.lastIndexOf(":") + 1);
        } else if (StringUtil.endWithAny(line, ">")) {// windows
            line = line.substring(0, line.length() - 1);
            line = line.substring(line.lastIndexOf("@"));
            dir = line.substring(line.lastIndexOf(" ") + 1);
            // windows的路径做一次处理
            dir = ShellFileUtil.fixFilePath(dir);
        }
        return dir;
    }


    /**
     * 初始化代理
     *
     * @param proxyConfig 代理配置
     * @return 代理对象
     */
    public static Proxy newProxy(ShellProxyConfig proxyConfig) {
        Proxy proxy = null;
        if (proxyConfig.isHttpProxy()) {
            ProxyHTTP http = new ProxyHTTP(proxyConfig.getHost(), proxyConfig.getPort());
            if (proxyConfig.isPasswordAuth()) {
                http.setUserPasswd(proxyConfig.getUser(), proxyConfig.getPassword());
            }
            proxy = http;
        } else if (proxyConfig.isSocks4Proxy()) {
            ProxySOCKS4 socks4 = new ProxySOCKS4(proxyConfig.getHost(), proxyConfig.getPort());
            if (proxyConfig.isPasswordAuth()) {
                socks4.setUserPasswd(proxyConfig.getUser(), proxyConfig.getPassword());
            }
            proxy = socks4;
        } else if (proxyConfig.isSocks5Proxy()) {
            ProxySOCKS5 socks5 = new ProxySOCKS5(proxyConfig.getHost(), proxyConfig.getPort());
            if (proxyConfig.isPasswordAuth()) {
                socks5.setUserPasswd(proxyConfig.getUser(), proxyConfig.getPassword());
            }
            proxy = socks5;
        }
        return proxy;
    }
}
