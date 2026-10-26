package cn.oyzh.easyshell.ssh;

import cn.oyzh.common.util.StringUtil;

/**
 * Exec 命令的 PATH 注入器。
 *
 * <p>注入是增强能力而不是执行前提：shell 无法识别、路径包含会破坏语法的字符或包装后过长时，
 * 必须原样返回命令。路径配置错误最多导致命令仍使用远端默认 PATH，不能让原本可执行的命令失败。</p>
 *
 * @author oyzh
 * @since 2026-10-25
 */
final class ShellExecCommandResolver {

    /**
     * Windows 命令行长度上限约为 8191，留出余量避免包装后截断命令。
     */
    private static final int MAX_WINDOWS_COMMAND_LENGTH = 8000;

    private ShellExecCommandResolver() {
    }

    /**
     * 远端 shell 家族
     */
    enum Family {
        CSH,
        CMD,
        POSIX,
        POWERSHELL,
        WINDOWS_POSIX,
        UNSUPPORTED
    }

    /**
     * 生成带 PATH 注入的 Exec 命令。
     *
     * @param command 原命令
     * @param path    预先拼接好的 PATH 前缀
     * @param family  远端 shell 家族
     * @return 包装后的命令；无法安全包装时返回原命令
     */
    static String resolve(String command, String path, Family family) {
        if (StringUtil.isBlank(command) || StringUtil.isBlank(path) || family == null) {
            return command;
        }
        if (!isSafeCommonPath(path)) {
            return command;
        }
        try {
            return switch (family) {
                case POSIX -> resolvePosix(command, path);
                case CSH -> resolveCsh(command, path);
                case CMD -> resolveCmd(command, path);
                case POWERSHELL -> resolvePowershell(command, path);
                case WINDOWS_POSIX -> resolveWindowsPosix(command, path);
                case UNSUPPORTED -> command;
            };
        } catch (RuntimeException ignored) {
            return command;
        }
    }

    /**
     * POSIX shell 在当前 shell 内临时扩展 PATH，保留 cd、alias、函数、管道等原命令语义。
     */
    private static String resolvePosix(String command, String path) {
        return "PATH=" + ShellSSHUtil.quoteCommand(path) + ":\"$PATH\"; export PATH; " + command;
    }

    /**
     * Windows POSIX shell 在当前 shell 内临时扩展 PATH，保留 cd、alias、函数、管道等原命令语义。
     */
    private static String resolveWindowsPosix(String command, String path) {
        return "PATH=" + ShellSSHUtil.quoteCommand(path) + ":\"$PATH\"; export PATH; MSYS2_ARG_CONV_EXCL='*' MSYS_NO_PATHCONV=1 " + command;
    }

    /**
     * csh/tcsh 不支持 POSIX 赋值语法，使用 setenv 扩展 PATH 后仍在原 shell 中执行命令。
     * 感叹号会触发 csh 历史展开，包含时放弃注入。
     */
    private static String resolveCsh(String command, String path) {
        if (path.indexOf('!') >= 0) {
            return command;
        }
        return "setenv PATH " + ShellSSHUtil.quoteCommand(path) + ":\"$PATH\"; " + command;
    }

    /**
     * cmd.exe 使用 set 包装。路径中的双引号、百分号和感叹号会参与解析，发现后放弃注入。
     */
    private static String resolveCmd(String command, String path) {
        if (path.indexOf('"') >= 0 || path.indexOf('%') >= 0 || path.indexOf('!') >= 0) {
            return command;
        }
        String resolved = "set \"PATH=" + path + ";%PATH%\" & " + command;
        return resolved.length() > MAX_WINDOWS_COMMAND_LENGTH ? command : resolved;
    }

    /**
     * PowerShell 使用单引号字符串，路径中的单引号通过双写转义，避免变量和子表达式展开。
     * PATH 设置被策略拦截时吞掉异常，确保原命令仍会执行。
     */
    private static String resolvePowershell(String command, String path) {
        String resolved = "try { $env:PATH=" + quotePowershell(path)
                + " + ';' + $env:PATH } catch {}; " + command;
        return resolved.length() > MAX_WINDOWS_COMMAND_LENGTH ? command : resolved;
    }

    /**
     * 拒绝会跨行或被 shell 当作终止符的路径内容。无法安全注入时宁可使用远端默认 PATH。
     */
    private static boolean isSafeCommonPath(String path) {
        return path.indexOf('\0') < 0 && path.indexOf('\r') < 0 && path.indexOf('\n') < 0;
    }

    private static String quotePowershell(String value) {
        return "'" + value.replace("'", "''") + "'";
    }
}
