package cn.oyzh.easyshell.ssh2;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.file.ShellFileUtil;
import cn.oyzh.ssh.util.SSHUtil;


/**
 * @author oyzh
 * @since 2025-03-21
 */
public class ShellSSHUtil {

    /**
     * 解析工作目录
     *
     * @param output  输出
     * @param homeDir 用户home目录
     * @return 工作目录
     */
    public static String resolveWorkerDir(String output, String homeDir) {
        if (StringUtil.isBlank(output) || !StringUtil.contains(output, "@")) {
            return null;
        }
        // 获取最后一行
        String line = output.lines().toList().getLast();
        if (StringUtil.isBlank(line) || !StringUtil.contains(line, "@")) {
            return null;
        }
        // 移除ansi字符串
        line = SSHUtil.removeAnsi(line);
        // 移除控制字符串
        line = SSHUtil.removeControl(line);
        // 针对部分情况下返回的ansi字符做处理
        if (line.endsWith("?25h")) {
            line = line.substring(0, line.length() - 6);
        } else if (line.endsWith("?2004h")) {
            line = line.substring(0, line.length() - 8);
        }
        // 目录
        String dir = null;
        // linux特殊
        if (StringUtil.endsWith(line, "]# ")) {
            line = line.substring(0, line.length() - 3);
            line = line.substring(line.lastIndexOf("["));
            line = line.substring(line.lastIndexOf("@"));
            dir = line.substring(line.lastIndexOf(" ") + 1);
            if (!"~".equals(dir) && !dir.startsWith("/")) {
                dir = "/" + dir;
            }
        } else if (StringUtil.endsWith(line, " # ")) {// unix
            line = line.substring(0, line.length() - 3);
            line = line.substring(line.lastIndexOf("@"));
            dir = line.substring(line.lastIndexOf(":") + 1);
        } else if (StringUtil.endsWithAny(line, "# ", "$ ")) {// linux
            line = line.substring(0, line.length() - 2);
            line = line.substring(line.lastIndexOf("@"));
            dir = line.substring(line.lastIndexOf(":") + 1);
        } else if (StringUtil.endWithAny(line, ">")) {// windows
            line = line.substring(0, line.length() - 1);
            line = line.substring(line.lastIndexOf("@"));
            dir = line.substring(line.lastIndexOf(" ") + 1);
            // 处理路径
            dir = ShellFileUtil.fixFilePath(dir);
        } else if (StringUtil.endWithAny(line, "% ")) {// macos
            line = line.substring(0, line.length() - 3);
            line = line.substring(line.lastIndexOf("@"));
            dir = line.substring(line.lastIndexOf(" ") + 1);
        }
        if (dir != null && homeDir != null) {
            // home目录
            if ("~".equals(dir)) {
                dir = homeDir;
            } else if (StringUtil.startWith(dir, "~")) {// home目录开头
                dir = ShellFileUtil.concat(homeDir, dir.substring(1));
            } else if (!StringUtil.startWith(dir, "/")) {// home目录可能会被省略
                dir = ShellFileUtil.concat(homeDir, dir);
            }
        }
        return dir;
    }

//    /**
//     * 解析命令
//     *
//     * @param output 输出
//     * @return 命令
//     */
//    public static String resolveCommand(String output) {
//        if (StringUtil.isBlank(output)) {
//            return null;
//        }
//        // 移除重复内容
//        if (output.length() > 1 && output.charAt(1) == '\b') {
//            output = output.substring(2);
//        }
//        // 移除换行字符
//        while (output.endsWith("\r") || output.endsWith("\n") || output.endsWith("\b")) {
//            output = output.substring(0, output.length() - 1).trim();
//        }
//        // 移除ansi字符串
//        output = SSHUtil.removeAnsi(output);
//        // 移除部分tty内容
//        if (output.endsWith("?2004l")) {
//            output = output.substring(0, output.length() - 8);
//        }
//        return output;
//    }

    // /**
    //  * 初始化代理
    //  *
    //  * @param proxyConfig 代理配置
    //  * @return 代理对象
    //  */
    // public static Proxy newProxy(ShellProxyConfig proxyConfig) {
    //     Proxy proxy = null;
    //     if (proxyConfig.isHttpProxy()) {
    //         ProxyHTTP http = new ProxyHTTP(proxyConfig.getHost(), proxyConfig.getPort());
    //         if (proxyConfig.isPasswordAuth()) {
    //             http.setUserPasswd(proxyConfig.getUser(), proxyConfig.getPassword());
    //         }
    //         proxy = http;
    //     } else if (proxyConfig.isSocks4Proxy()) {
    //         ProxySOCKS4 socks4 = new ProxySOCKS4(proxyConfig.getHost(), proxyConfig.getPort());
    //         if (proxyConfig.isPasswordAuth()) {
    //             socks4.setUserPasswd(proxyConfig.getUser(), proxyConfig.getPassword());
    //         }
    //         proxy = socks4;
    //     } else if (proxyConfig.isSocks5Proxy()) {
    //         ProxySOCKS5 socks5 = new ProxySOCKS5(proxyConfig.getHost(), proxyConfig.getPort());
    //         if (proxyConfig.isPasswordAuth()) {
    //             socks5.setUserPasswd(proxyConfig.getUser(), proxyConfig.getPassword());
    //         }
    //         proxy = socks5;
    //     }
    //     return proxy;
    // }

    // /**
    //  * 获取ssh agent的sock文件
    //  *
    //  * @return 结果
    //  */
    // public static String getSSHAgentSockFile() {
    //     String sockFile = null;
    //     String file = System.getenv("SSH_AUTH_SOCK");
    //     boolean findMore = false;
    //     if (FileUtil.exist(file)) {
    //         // 注意，这个SSH_AUTH_SOCK值未必准确，可能需要深入查找
    //         String res = RuntimeUtil.execForStr("ssh-add -l");
    //         if (StringUtil.contains(res, "The agent has no identities")) {
    //             findMore = true;
    //         } else {
    //             sockFile = file;
    //         }
    //     } else {
    //         findMore = true;
    //     }
    //     // 寻找更深层次的文件
    //     if (findMore && OSUtil.isMacOS()) {
    //         String tmpdir = System.getenv("TMPDIR");
    //         File tmp = new File(tmpdir);
    //         if (tmp.exists() && tmp.isDirectory()) {
    //             File[] files2 = tmp.listFiles();
    //             if (files2 != null) {
    //                 f1:
    //                 for (File file1 : files2) {
    //                     if (file1.isDirectory() && file1.getName().startsWith("ssh-")) {
    //                         File[] files3 = file1.listFiles();
    //                         if (files3 != null) {
    //                             for (File file2 : files3) {
    //                                 sockFile = file2.getPath();
    //                                 break f1;
    //                             }
    //                         }
    //                     }
    //                 }
    //             }
    //         }
    //     }
    //     return sockFile;
    // }
}
