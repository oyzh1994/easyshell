package cn.oyzh.easyshell.ssh;

import cn.oyzh.common.util.StringUtil;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

/**
 * 为交互式 shell 安装 OSC 工作目录上报能力。
 * <p>
 * 默认 shell 不会主动发送 OSC 7/9;9，需要安装一个仅对当前 shell 会话生效的 prompt hook。
 * bash/zsh 使用标准 OSC 7；FreeBSD sh/csh 和 Windows cmd/PowerShell 使用 OSC 9;9，
 * 直接上报原始绝对路径，避免 shell 缺少可靠的 URI 编码能力时路径失真。
 * hook 不修改用户配置文件，shell 退出后自动失效。
 *
 * @author oyzh
 * @since 2026-10-24
 */
public final class ShellOscShellIntegration {

    /**
     * cmd.exe 当前目录上报命令。$E 为 ESC，$P 为当前目录，末尾保留默认的路径提示符。
     */
    private static final String CMD_OSC_HOOK = "prompt $E]9;9;$P$E\\$P$G";

    /**
     * PowerShell 当前目录上报命令。保留原 prompt，并把 hook 自身排除在命令历史之外。
     */
    private static final String POWERSHELL_OSC_HOOK =
            "if (-not $global:__easyshell_osc7_installed__) { "
                    + "$global:__rtPrompt=$function:prompt; "
                    + "function global:prompt { $r=& $global:__rtPrompt; "
                    + "\"$([char]27)]9;9;$(Get-Location)$([char]27)\\$r\" }; "
                    + "try { Set-PSReadLineOption -AddToHistoryHandler "
                    + "{ param($l) $l -notmatch '__rt' } -ErrorAction SilentlyContinue } catch {}; "
                    + "$global:__easyshell_osc7_installed__=$true }";

    /**
     * PowerShell 用 ReadKey 无回显读取下一行 hook，避免 PSReadLine 将长命令高亮回显。
     */
    private static final String POWERSHELL_READ_HOOK =
            "$s='';while(($k=[Console]::ReadKey($true)).Key -ne 13){$s+=$k.KeyChar};iex $s #__rt";

    /**
     * 第一条命令会先在远端关闭回显并保持 1 秒，给后续命令留出无回显写入窗口。
     */
    private static final long ECHO_SETTLE_MILLIS = 350L;

    /**
     * 等待 PowerShell 进入 ReadKey 循环。
     */
    private static final long POWERSHELL_READ_HOOK_MILLIS = 500L;

    private ShellOscShellIntegration() {
    }

    /**
     * 远端平台类型。不同平台的默认 shell 和 prompt 能力不同。
     */
    public enum Platform {
        UNIX,
        FREEBSD,
        WINDOWS
    }

    /**
     * 是否支持该 shell
     *
     * @param shellType shell类型
     * @param platform  平台类型
     * @return 结果
     */
    public static boolean isSupported(String shellType, Platform platform) {
        return command(shellType, platform) != null;
    }

    /**
     * 是否为 Windows 原生 shell。Windows 上的 Git Bash 使用 POSIX 路径，
     * 不能直接作为 Windows 文件路径使用，继续保留提示符回退。
     *
     * @param shellType shell类型
     * @return 结果
     */
    public static boolean isWindowsShell(String shellType) {
        String shellName = shellName(shellType);
        return "cmd".equals(shellName) || "cmd.exe".equals(shellName)
                || "powershell".equals(shellName) || "powershell.exe".equals(shellName)
                || "pwsh".equals(shellName) || "pwsh.exe".equals(shellName);
    }

    /**
     * 安装 OSC 工作目录上报 hook
     *
     * @param output    终端输出流
     * @param shellType shell类型
     * @param platform  平台类型
     * @return 是否已安装
     * @throws IOException IO异常
     */
    public static boolean install(OutputStream output, String shellType, Platform platform) throws IOException {
        Command command = command(shellType, platform);
        if (command == null) {
            return false;
        }
        // Windows 控制台没有 stty，hook 本身就是一条可重复执行的 prompt 命令。
        if (command.disableEcho() == null) {
            if (isPowerShellShell(shellName(shellType))) {
                output.write((POWERSHELL_READ_HOOK + "\r").getBytes(StandardCharsets.US_ASCII));
                output.flush();
                sleep(POWERSHELL_READ_HOOK_MILLIS);
            }
            output.write((command.hook() + "\r").getBytes(StandardCharsets.US_ASCII));
            output.flush();
            return true;
        }
        // 第一条命令会短暂显示，后续命令在远端 ECHO 关闭窗口内发送
        output.write((command.disableEcho() + "\r").getBytes(StandardCharsets.US_ASCII));
        output.flush();
        sleep(ECHO_SETTLE_MILLIS);
        // 先清理第一条命令和提示符，再安装 hook；恢复命令单独一行，hook 语法失败时仍可恢复回显
        String install = command.prepare() + "\r"
                + command.hook() + "\r"
                + command.restoreEcho() + "\r";
        output.write(install.getBytes(StandardCharsets.US_ASCII));
        output.flush();
        return true;
    }

    private static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * 是否为 PowerShell
     *
     * @param shellName shell名称
     * @return 结果
     */
    private static boolean isPowerShellShell(String shellName) {
        return "powershell".equals(shellName) || "powershell.exe".equals(shellName)
                || "pwsh".equals(shellName) || "pwsh.exe".equals(shellName);
    }

    /**
     * 获取shell对应的安装命令
     *
     * @param shellType shell类型
     * @param platform  平台类型
     * @return 命令，不支持时返回null
     */
    static Command command(String shellType, Platform platform) {
        String shellName = shellName(shellType);
        if (shellName == null || platform == null) {
            return null;
        }
        return switch (platform) {
            case WINDOWS -> windowsCommand(shellName);
            case FREEBSD -> freeBsdCommand(shellName);
            case UNIX -> unixCommand(shellName);
        };
    }

    /**
     * 获取 Windows shell 的安装命令
     */
    private static Command windowsCommand(String shellName) {
        return switch (shellName) {
            case "cmd", "cmd.exe" -> new Command(null, null, CMD_OSC_HOOK, null);
            case "powershell", "powershell.exe", "pwsh", "pwsh.exe" ->
                    new Command(null, null, POWERSHELL_OSC_HOOK, null);
            default -> null;
        };
    }

    /**
     * 获取 FreeBSD shell 的安装命令。FreeBSD 的 /bin/csh 实际为 tcsh，
     * /bin/sh 支持在 PS1 中使用 \e、\w、\a 等提示符转义。
     */
    private static Command freeBsdCommand(String shellName) {
        if ("sh".equals(shellName)) {
            return freeBsdShCommand();
        }
        return unixCommand(shellName);
    }

    /**
     * 获取通用 Unix shell 的安装命令
     */
    private static Command unixCommand(String shellName) {
        return switch (shellName) {
            case "bash" -> bashCommand();
            case "zsh" -> zshCommand();
            case "csh", "tcsh" -> tcshCommand();
            default -> null;
        };
    }

    /**
     * Bash 使用 PROMPT_COMMAND 上报目录。
     */
    private static Command bashCommand() {
        return new Command(
                    // 首条命令会进入历史，关闭历史后其余安装命令不会再被记录；
                    // 最后的清理命令按 __easyshell_osc7_hist__ 标记只删除这一条。
                    " __easyshell_hist_on__=; [[ -o history ]] && __easyshell_hist_on__=1; "
                            + "set +o history; stty -echo; sleep 1; __easyshell_osc7_hist__=1",
                    clearPromptAndPreviousLine(),
                    "__easyshell_osc7__(){ local p=${PWD//'%'/%25}; "
                            + "printf '\\033]7;file://%s%s\\033\\\\' \"${HOSTNAME:-localhost}\" \"$p\"; }; "
                            + "if [[ $(declare -p PROMPT_COMMAND 2>/dev/null) == 'declare -a'* ]]; then "
                            + "case \" ${PROMPT_COMMAND[*]} \" in *\" __easyshell_osc7__ \"*) ;; "
                            + "*) PROMPT_COMMAND=(__easyshell_osc7__ \"${PROMPT_COMMAND[@]}\");; esac; "
                            + "else case \";$PROMPT_COMMAND;\" in *\";__easyshell_osc7__;\"*) ;; "
                            + "*) PROMPT_COMMAND=\"__easyshell_osc7__${PROMPT_COMMAND:+;$PROMPT_COMMAND}\";; esac; fi",
                    // history -d 会重排序号，因此先从 history 1 取出准确序号并确认是本安装命令。
                    // Bash 3.2 删除历史后退出时会重复写入旧历史，只在 Bash 4+ 或没有历史文件时删除。
                    "PS1=$__easyshell_ps1__; unset __easyshell_ps1__; stty echo; "
                            + "if [ \"$__easyshell_hist_on__\" = 1 ]; then "
                            + "if [ \"${BASH_VERSINFO[0]:-0}\" -ge 4 ] || [ ! -s \"${HISTFILE:-}\" ]; then "
                            + "__easyshell_hist_last__=$(HISTTIMEFORMAT= history 1); "
                            + "case \"$__easyshell_hist_last__\" in "
                            + "*__easyshell_hist_on__*__easyshell_osc7_hist__*) "
                            + "read -r __easyshell_hist_index__ __easyshell_hist_rest__ "
                            + "<<< \"$__easyshell_hist_last__\"; "
                            + "history -d \"$__easyshell_hist_index__\";; esac; fi; "
                            + "set -o history; fi; "
                            + "unset __easyshell_hist_on__ __easyshell_hist_last__ "
                            + "__easyshell_hist_index__ __easyshell_hist_rest__ __easyshell_osc7_hist__");
    }

    /**
     * Zsh 使用 precmd hook 上报目录。
     */
    private static Command zshCommand() {
        return new Command(
                    // zsh 没有 history -d。fc -p 将后续安装命令隔离到临时历史，fc -P 恢复；
                    // 清理行再设置 HISTORY_IGNORE，使首条安装命令不会写入历史文件。
                    " fc -p; __easyshell_osc7_hist__=1; "
                            + "__easyshell_histignore_space__=; [[ -o histignorespace ]] "
                            + "&& __easyshell_histignore_space__=1; "
                            + "__easyshell_zle_on__=; [[ -o zle ]] && __easyshell_zle_on__=1; "
                            + "unsetopt zle; stty -echo; sleep 1",
                    clearPromptAndPreviousLine(),
                    "__easyshell_osc7__(){ local p=${PWD//'%'/%25}; "
                            + "printf '\\033]7;file://%s%s\\033\\\\' \"${HOST:-localhost}\" \"$p\"; }; "
                            + "autoload -Uz add-zsh-hook 2>/dev/null; "
                            + "if (( $+functions[add-zsh-hook] )); then "
                            + "add-zsh-hook precmd __easyshell_osc7__ 2>/dev/null; "
                            + "else precmd_functions+=(__easyshell_osc7__); fi",
                    "PS1=$__easyshell_ps1__; unset __easyshell_ps1__; stty echo; "
                            + "[[ \"$__easyshell_zle_on__\" == 1 ]] && setopt zle; fc -P; "
                            + "if [ \"$__easyshell_histignore_space__\" != 1 ]; then "
                            + "if [ -n \"$HISTORY_IGNORE\" ]; then "
                            + "HISTORY_IGNORE=\"($HISTORY_IGNORE|*__easyshell_osc7_hist__*)\"; "
                            + "else HISTORY_IGNORE='*__easyshell_osc7_hist__*'; fi; fi; "
                            + "unset __easyshell_histignore_space__ __easyshell_zle_on__ "
                            + "__easyshell_osc7_hist__");
    }

    /**
     * tcsh/csh 使用 cwdcmd hook 上报目录，并保留用户已有的 cwdcmd。
     */
    private static Command tcshCommand() {
        String report = "printf \"\\033]9;9;%s\\033\\\\\" \"$cwd\"";
        String hook = "set __easyshell_previous_cwdcmd__=\"`alias cwdcmd`\"; "
                + "if (\"$__easyshell_previous_cwdcmd__\" == '') "
                + "set __easyshell_previous_cwdcmd__='true'; "
                + "alias __easyshell_previous_cwdcmd__ \"$__easyshell_previous_cwdcmd__\"; "
                + "alias __easyshell_osc7__ '" + report + "'; "
                + "alias cwdcmd '__easyshell_osc7__; __easyshell_previous_cwdcmd__'; "
                + report;
        return new Command(
                // tcsh 没有按条删除历史的命令，修改 history 变量会清空整个会话历史。
                // 因而不触碰用户历史，仅关闭回显来隐藏安装过程。
                " stty -echo; sleep 1",
                "printf '\\033[2K\\r\\033[1A\\033[2K\\r'; "
                        + "set __easyshell_prompt__=\"$prompt\"; set prompt=\"\"",
                hook,
                "set prompt=\"$__easyshell_prompt__\"; unset __easyshell_prompt__; stty echo");
    }

    /**
     * FreeBSD sh 的 PS1 原生支持 \e、\w、\a，无需 prompt hook 或修改历史配置。
     * 使用 OSC 9;9 上报原始路径，避免 % 等字符需要 URI 转义。
     */
    private static Command freeBsdShCommand() {
        return new Command(
                " stty -echo; sleep 1",
                clearPromptAndPreviousLine(),
                "__easyshell_ps1__='\\e]9;9;\\w\\a'\"$__easyshell_ps1__\"",
                "PS1=$__easyshell_ps1__; unset __easyshell_ps1__; stty echo");
    }

    /**
     * 清理当前提示符和上一条命令回显，并在安装期间隐藏提示符。
     *
     * @return 命令
     */
    private static String clearPromptAndPreviousLine() {
        return "printf '\\033[2K\\r\\033[1A\\033[2K\\r'; __easyshell_ps1__=$PS1; PS1=";
    }

    /**
     * 提取shell名称
     *
     * @param shellType shell类型
     * @return 名称
     */
    private static String shellName(String shellType) {
        if (StringUtil.isBlank(shellType)) {
            return null;
        }
        String name = shellType.strip().replace('\\', '/');
        int slash = name.lastIndexOf('/');
        if (slash >= 0) {
            name = name.substring(slash + 1);
        }
        while (name.startsWith("-")) {
            name = name.substring(1);
        }
        name = name.toLowerCase(Locale.ROOT);
        return switch (name) {
            case "bash", "zsh", "sh", "csh", "tcsh",
                    "cmd", "cmd.exe", "powershell", "powershell.exe", "pwsh", "pwsh.exe" -> name;
            default -> null;
        };
    }

    /**
     * shell集成命令
     *
     * @param disableEcho 关闭回显并保持无回显窗口
     * @param prepare     清理安装痕迹并保存提示符
     * @param hook        安装hook
     * @param restoreEcho 恢复提示符和回显
     */
    record Command(String disableEcho, String prepare, String hook, String restoreEcho) {
    }
}
