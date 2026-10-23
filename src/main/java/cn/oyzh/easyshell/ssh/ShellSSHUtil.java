package cn.oyzh.easyshell.ssh;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.ShellConst;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.domain.ShellKey;
import cn.oyzh.easyshell.domain.ShellProxyConfig;
import cn.oyzh.easyshell.file.ShellFileUtil;
import cn.oyzh.easyshell.ssh.server.ShellServerExec;
import cn.oyzh.easyshell.store.ShellKeyStore;
import cn.oyzh.easyshell.util.ShellViewFactory;
import cn.oyzh.fx.plus.util.FXUtil;
import cn.oyzh.fx.plus.window.StageAdapter;
import cn.oyzh.ssh.domain.SSHConnect;
import cn.oyzh.ssh.util.SSHUtil;
import cn.oyzh.ssh.util.JschUtil;
import com.jcraft.jsch.Proxy;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * ssh工具类，提供工作目录解析、认证失败处理与连接转换等功能
 *
 * @author oyzh
 * @since 2025-03-21
 */
public class ShellSSHUtil {

    private static final String KNOWN_HOSTS_PATH_PROPERTY = "easyshell.known_hosts.path";

    public static final String HEARTBEAT_INTERVAL_EXTRA = "heartbeatInterval";

    public static final int DEFAULT_HEARTBEAT_INTERVAL = 60;

    public static final int MAX_HEARTBEAT_INTERVAL = 3600;

    public static void setHeartbeatInterval(ShellConnect connect, int seconds) {
        connect.putExtra(HEARTBEAT_INTERVAL_EXTRA, seconds);
    }

    public static int getHeartbeatInterval(ShellConnect connect) {
        Object value = connect == null ? null : connect.getExtra(HEARTBEAT_INTERVAL_EXTRA);
        int interval;
        if (value instanceof Number number) {
            interval = number.intValue();
        } else if (value != null) {
            try {
                interval = Integer.parseInt(value.toString());
            } catch (NumberFormatException ex) {
                return DEFAULT_HEARTBEAT_INTERVAL;
            }
        } else {
            return DEFAULT_HEARTBEAT_INTERVAL;
        }
        return interval >= 0 && interval <= MAX_HEARTBEAT_INTERVAL ? interval : DEFAULT_HEARTBEAT_INTERVAL;
    }

    public static Proxy newProxy(ShellProxyConfig proxyConfig) {
        return JschUtil.newProxy(proxyConfig);
    }

    /**
     * osc转义序列（如终端标题、超链接），removeAnsi无法处理
     */
    private static final Pattern OSC_PATTERN = Pattern.compile("\u001B\\][^\u0007\u001B]*(?:\u0007|\u001B\\\\)");

    /**
     * 私有模式、字符集等残留的转义序列，如ESC[?25h、ESC[?2004h、ESC=
     */
    private static final Pattern ESCAPE_PATTERN = Pattern.compile("\u001B(?:\\[[?=>][0-9;]*[a-zA-Z]|[=>]|\\([0-9A-Za-z])");

    /**
     * 方括号形式的提示符，如[user@host dir]#、[user@host dir]$
     */
    private static final Pattern BRACKET_PROMPT_PATTERN = Pattern.compile(".*\\[([^\\[\\]]*@[^\\[\\]]*)\\][#$%\u276F]\\s*$");

    /**
     * 提示符末尾的额外修饰，如git分支信息"(master)"、"[main]"、"git:(master)"
     */
    private static final Pattern DECORATION_PATTERN = Pattern.compile("\\s*(?:git:\\([^()]*\\)|\\([^()]*\\)|\\[[^\\[\\]]*\\]|\\{[^{}]*\\})+$");

    /**
     * 解析工作目录
     *
     * @param output  输出
     * @param homeDir 用户home目录
     * @return 工作目录
     */
    public static String resolveWorkerDir(String output, String homeDir) {
        if (StringUtil.isBlank(output)) {
            return null;
        }
        List<String> lines = output.lines().toList();
        boolean last = true;
        // 从后往前查找，取最近一个可解析的提示符
        for (int i = lines.size() - 1; i >= 0; i--) {
            String line = cleanPromptLine(lines.get(i));
            if (StringUtil.isBlank(line)) {
                continue;
            }
            String dir = resolvePromptLine(line, homeDir, last);
            if (dir != null) {
                return dir;
            }
            last = false;
        }
        return null;
    }

    /**
     * 解析单行提示符
     *
     * @param line     提示符行
     * @param homeDir  用户home目录
     * @param allowBare 是否允许解析无提示符字符的提示符
     * @return 工作目录
     */
    private static String resolvePromptLine(String line, String homeDir, boolean allowBare) {
        // 目录
        String dir = null;
        // linux特殊，如[user@host dir]#、[user@host dir]$
        Matcher matcher = BRACKET_PROMPT_PATTERN.matcher(line);
        if (matcher.matches()) {
            dir = parseWorkerDir(matcher.group(1));
            // 方括号形式的提示符通常省略前导斜杠，补全为绝对路径
            if (dir != null && !"~".equals(dir) && !isAbsolutePath(dir)) {
                dir = "/" + dir;
            }
        }
        // 普通提示符，如user@host:dir#、user@host dir$、user@host dir%、C:\dir>、PS C:\dir>
        if (dir == null) {
            String body = stripPromptSuffix(line);
            if (body != null) {
                dir = parseWorkerDir(body);
                if (dir == null) {
                    dir = parseWindowsDir(body);
                }
            }
        }
        // 部分主题（如powerlevel10k）的提示符末尾没有提示符字符，仅在路径特征明显时才解析
        if (dir == null && allowBare) {
            dir = parseBareDir(line);
        }
        // 仅包含目录的提示符，如~#、host:~#、[~/proj]$（没有user@host）
        if (dir == null) {
            dir = parseDirOnlyPrompt(line);
        }
        if (StringUtil.isBlank(dir)) {
            return null;
        }
        // 移除提示符中的额外修饰，如git分支信息
        dir = stripDecoration(dir);
        if (StringUtil.isBlank(dir)) {
            return null;
        }
        // windows路径统一
        if (isWindowsPath(dir)) {
            dir = ShellFileUtil.fixFilePath(dir);
        }
        // home目录
        if (homeDir != null && !isAbsolutePath(dir)) {
            if ("~".equals(dir)) {
                dir = homeDir;
            } else if (StringUtil.startWith(dir, "~/")) {// home目录开头
                dir = ShellFileUtil.concat(homeDir, dir.substring(1));
            } else if (!StringUtil.startWith(dir, "~")) {// home目录可能会被省略
                dir = ShellFileUtil.concat(homeDir, dir);
            }
        }
        return dir;
    }

    /**
     * 清理提示符行中的转义及控制字符
     *
     * @param line 原始行
     * @return 清理后的行
     */
    private static String cleanPromptLine(String line) {
        line = OSC_PATTERN.matcher(line).replaceAll("");
        line = SSHUtil.removeAnsi(line);
        line = ESCAPE_PATTERN.matcher(line).replaceAll("");
        return SSHUtil.removeControl(line);
    }

    /**
     * 移除提示符末尾的提示符字符
     *
     * @param line 提示符行
     * @return 移除后的内容，不是提示符时返回null
     */
    private static String stripPromptSuffix(String line) {
        int end = line.length();
        while (end > 0 && Character.isWhitespace(line.charAt(end - 1))) {
            end--;
        }
        if (end == 0) {
            return null;
        }
        char suffix = line.charAt(end - 1);
        if (suffix != '#' && suffix != '$' && suffix != '%' && suffix != '>' && suffix != '\u276F' && suffix != '\u03BB') {
            return null;
        }
        return line.substring(0, end - 1).strip();
    }

    /**
     * 解析用户主机段中的目录
     *
     * @param prompt 用户主机段，如user@host:dir、user@host dir
     * @return 目录
     */
    private static String parseWorkerDir(String prompt) {
        int at = prompt.lastIndexOf('@');
        if (at < 0) {
            return null;
        }
        String rest = prompt.substring(at + 1);
        // 跳过ipv6地址中的冒号，如user@[::1]:/root
        int offset = 0;
        if (StringUtil.startWith(rest, "[")) {
            int end = rest.indexOf(']');
            if (end < 0) {
                return null;
            }
            offset = end + 1;
        }
        int colon = rest.indexOf(':', offset);
        int space = rest.indexOf(' ', offset);
        // user@host:dir
        if (colon >= 0 && (space < 0 || colon < space)) {
            return trimDir(rest.substring(colon + 1));
        }
        // user@host dir
        if (space >= 0) {
            return trimDir(rest.substring(space + 1));
        }
        return null;
    }

    /**
     * 修正目录首尾空白
     *
     * @param dir 目录
     * @return 修正后的目录
     */
    private static String trimDir(String dir) {
        dir = dir.strip();
        return dir.isEmpty() ? null : dir;
    }

    /**
     * 解析windows提示符中的目录，如C:\dir、PS C:\dir、user@host C:\dir
     *
     * @param prompt 提示符内容（已移除提示符字符）
     * @return 目录
     */
    private static String parseWindowsDir(String prompt) {
        String body = prompt.strip();
        // powershell前缀
        if (StringUtil.startWithIgnoreCase(body, "PS ")) {
            body = body.substring(3).strip();
        }
        // user@host前缀
        int at = body.lastIndexOf('@');
        if (at >= 0) {
            int space = body.indexOf(' ', at);
            if (space >= 0) {
                body = body.substring(space + 1).strip();
            }
        }
        return isWindowsPath(body) ? body : null;
    }

    /**
     * 解析无提示符字符的提示符中的目录，仅采用路径特征明显的值
     *
     * @param line 提示符行
     * @return 目录
     */
    private static String parseBareDir(String line) {
        String prompt = stripDecoration(line).strip();
        int at = prompt.lastIndexOf('@');
        if (at < 0) {
            return null;
        }
        String rest = prompt.substring(at + 1);
        int offset = 0;
        if (StringUtil.startWith(rest, "[")) {
            int end = rest.indexOf(']');
            if (end < 0) {
                return null;
            }
            offset = end + 1;
        }
        int space = rest.indexOf(' ', offset);
        int colon = rest.indexOf(':', offset);
        String dir;
        if (colon >= 0 && (space < 0 || colon < space)) {
            dir = rest.substring(colon + 1).strip();
        } else if (space >= 0) {
            dir = rest.substring(space + 1).strip();
        } else {
            return null;
        }
        // 无提示符字符时可能把已输入的命令误判为目录，需排除含空白的值
        if (dir.isEmpty() || dir.indexOf(' ') >= 0 || dir.indexOf('\t') >= 0) {
            return null;
        }
        if (StringUtil.startWithAny(dir, "~", "/", "./", "../") || isWindowsPath(dir)) {
            return dir;
        }
        return null;
    }

    /**
     * 解析仅包含目录的提示符，如~#、~/proj$、host:~#、[~]#、[/etc]$
     *
     * @param line 提示符行
     * @return 目录
     */
    private static String parseDirOnlyPrompt(String line) {
        String body = stripPromptSuffix(line);
        if (body == null) {
            return null;
        }
        body = body.strip();
        // 移除提示符外的括号
        while (body.length() >= 2 && (body.startsWith("[") && body.endsWith("]")
                || body.startsWith("(") && body.endsWith(")")
                || body.startsWith("{") && body.endsWith("}"))) {
            body = body.substring(1, body.length() - 1).strip();
        }
        if (body.isEmpty()) {
            return null;
        }
        // windows路径
        if (isWindowsPath(body)) {
            return body;
        }
        // hostname:dir
        int colon = body.indexOf(':');
        if (colon > 0 && body.indexOf(' ') < 0 && !StringUtil.startWith(body, "/")) {
            String host = body.substring(0, colon);
            if (!StringUtil.contains(host, "/")) {
                String dir = body.substring(colon + 1).strip();
                return isPathLike(dir) ? dir : null;
            }
        }
        // 目录本身就是明显路径时才采用，避免把普通输出行当作目录
        return isPathLike(body) ? body : null;
    }

    /**
     * 是否为明显的路径
     *
     * @param path 路径
     * @return 结果
     */
    private static boolean isPathLike(String path) {
        if (StringUtil.isBlank(path)) {
            return false;
        }
        return StringUtil.startWithAny(path, "~", "/", "./", "../") || isWindowsPath(path);
    }

    /**
     * 移除目录末尾的修饰信息
     *
     * @param dir 目录
     * @return 移除后的目录
     */
    private static String stripDecoration(String dir) {
        dir = dir.strip();
        String prev;
        do {
            prev = dir;
            dir = DECORATION_PATTERN.matcher(dir).replaceFirst("").strip();
        } while (!dir.equals(prev));
        // 清理不完整/连续的方括号修饰，如dir][git]
        while (dir.endsWith("]")) {
            dir = dir.substring(0, dir.length() - 1).strip();
        }
        return dir;
    }

    /**
     * 是否为windows路径
     *
     * @param path 路径
     * @return 结果
     */
    private static boolean isWindowsPath(String path) {
        if (StringUtil.isBlank(path)) {
            return false;
        }
        if (path.length() >= 2 && path.charAt(1) == ':' && Character.isLetter(path.charAt(0))) {
            return true;
        }
        return StringUtil.startWith(path, "\\\\") || StringUtil.startWith(path, "\\");
    }

    /**
     * 是否为绝对路径
     *
     * @param path 路径
     * @return 结果
     */
    private static boolean isAbsolutePath(String path) {
        return StringUtil.startWith(path, "/") || isWindowsPath(path);
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

    /**
     * 认证失败
     *
     * @param connect 连接
     * @return 处理后的连接
     */
    public static ShellConnect onVerifyFailure(ShellConnect connect) {
        AtomicReference<ShellConnect> reference = new AtomicReference<>();
        FXUtil.runWait(() -> {
            StageAdapter adapter = ShellViewFactory.sshAuth(connect);
            if (adapter != null) {
                ShellConnect connect1 = adapter.getProp("connect");
                reference.set(connect1);
            }
        });
        return reference.get();
    }

//    /**
//     * 认证失败
//     *
//     * @param connect 连接
//     * @return 处理后的连接
//     */
//    public static SSHConnect onVerifyFailure(SSHConnect connect) {
//        ShellConnect shellConnect = convert(connect);
//        ShellConnect shellConnect1 = onVerifyFailure(shellConnect);
//        return convert(shellConnect1);
//    }

    /**
     * 获取已知主机路径
     *
     * @return 已知主机路径
     */
    public static Path getKnownHostsPath() {
        String override = System.getProperty(KNOWN_HOSTS_PATH_PROPERTY);
        if (StringUtil.isNotBlank(override)) {
            return Path.of(override);
        }
        return Path.of(ShellConst.getStorePath(), "known_hosts");
    }

    /**
     * 转换对象
     *
     * @param connect 连接
     * @return 转换后的对象
     */
    public static SSHConnect convert(ShellConnect connect) {
        if (connect == null) {
            return null;
        }
        SSHConnect sshConnect = new SSHConnect();
        sshConnect.setHost(connect.hostIp());
        sshConnect.setUser(connect.getUser());
        sshConnect.setPort(connect.hostPort());
        sshConnect.setPassword(connect.getPassword());
        sshConnect.setTimeout(connect.getConnectTimeOut());
        sshConnect.setCertificatePath(connect.getCertificate());
        sshConnect.setCertificatePwd(connect.getCertificatePwd());
        if (connect.isManagerAuth()) {
            ShellKeyStore keyStore = ShellKeyStore.INSTANCE;
            ShellKey key = keyStore.selectOne(connect.getKeyId());
            sshConnect.setAuthMethod("key");
            sshConnect.setCertificatePwd(key.getPassword());
            sshConnect.setCertificatePubKey(key.getPublicKey());
            sshConnect.setCertificatePriKey(key.getPrivateKey());
        } else if (connect.isCertificateAuth()) {
            sshConnect.setAuthMethod("certificate");
        } else if (connect.isPasswordAuth()) {
            sshConnect.setAuthMethod("password");
        } else if (connect.isSSHAgentAuth()) {
            sshConnect.setAuthMethod("sshAgent");
        }
        return sshConnect;
    }

    /**
     * 转换对象
     *
     * @param connect 连接
     * @return 转换后的对象
     */
    public static ShellConnect convert(SSHConnect connect) {
        if (connect == null) {
            return null;
        }
        ShellConnect sshConnect = new ShellConnect();
        sshConnect.setHost(connect.getHost() + ":" + connect.getPort());
        sshConnect.setUser(connect.getUser());
        sshConnect.setPassword(connect.getPassword());
        sshConnect.setConnectTimeOut(connect.getTimeoutSecond());
        sshConnect.setCertificate(connect.getCertificatePath());
        sshConnect.setCertificatePwd(connect.getCertificatePwd());
        if (connect.isKeyAuth()) {
            ShellKeyStore keyStore = ShellKeyStore.INSTANCE;
            ShellKey key = new ShellKey();
            key.setPassword(connect.getCertificatePwd());
            key.setPublicKey(connect.getCertificatePubKey());
            key.setPrivateKey(connect.getCertificatePriKey());
            keyStore.insert(key);
            sshConnect.setAuthMethod("manager");
            sshConnect.setKeyId(key.getId());
        } else if (connect.isCertificateAuth()) {
            sshConnect.setAuthMethod("certificate");
        } else if (connect.isPasswordAuth()) {
            sshConnect.setAuthMethod("password");
        } else if (connect.isSSHAgentAuth()) {
            sshConnect.setAuthMethod("sshAgent");
        }
        return sshConnect;
    }

    /**
     * 历史记录
     *
     * @param client 客户端
     * @param kw     关键字
     * @param limit  限制
     */
    public static List<String> histories(ShellSSHClient client, String kw, int limit) {
        // 服务执行者
        ShellServerExec exec = client.serverExec();
        // 持久化命令
        exec.persistentCommand();
        // 获取最近200条历史
        return exec.history(limit, kw);
    }

    /**
     * 是否相同的路径
     *
     * @param path1 路径1
     * @param path2 路径2
     * @return 结果
     */
    public static boolean isSamePath(String path1, String path2) {
        path1 = correctPath(path1);
        path2 = correctPath(path2);
        return StringUtil.equals(path1, path2);
    }

    /**
     * 纠正路径
     *
     * @param path1 路径1
     * @return 结果
     */
    public static String correctPath(String path1) {
        if (StringUtil.isNotEmpty(path1) && path1.length() > 1 && path1.endsWith("/")) {
            path1 = StringUtil.removeLast(path1);
        }
        return path1;
    }
}
