package cn.oyzh.easyshell.terminal;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.system.OSUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.domain.ShellSetting;
import cn.oyzh.easyshell.store.ShellSettingStore;
import cn.oyzh.fx.tty.TtyHyperlinkFilter;
import cn.oyzh.fx.tty.TtyProcessTtyConnector;
import cn.oyzh.fx.tty.TtyTermWidget;
import cn.oyzh.fx.tty.zmodem.TtyZModemTtyConnector;
import com.jediterm.terminal.CursorShape;
import com.jediterm.terminal.TtyConnector;
import com.jediterm.terminal.ui.settings.SettingsProvider;
import com.pty4j.PtyProcess;
import com.pty4j.PtyProcessBuilder;
import kotlin.text.Charsets;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @author oyzh
 * @since 2025-03-04
 */
public class ShellDefaultTermWidget extends TtyTermWidget {

    /**
     * 设置
     */
    protected final ShellSetting setting = ShellSettingStore.SETTING;

    public ShellDefaultTermWidget() {
        this(new ShellSettingsProvider());
    }

    public ShellDefaultTermWidget(SettingsProvider provider) {
        super(provider);
        if (this.setting.isTermParseHyperlink()) {
            this.addHyperlinkFilter(new TtyHyperlinkFilter());
        }
        // 初始化光标
        if (this.setting.getTermCursorBlinks() > 0) {
            switch (this.setting.getTermCursorStyle()) {
                case 1:
                    this.getTerminalPanel().setCursorShape(CursorShape.BLINK_UNDERLINE);
                    break;
                case 2:
                    this.getTerminalPanel().setCursorShape(CursorShape.BLINK_VERTICAL_BAR);
                    break;
                default:
                    this.getTerminalPanel().setCursorShape(CursorShape.BLINK_BLOCK);
                    break;
            }
        } else {
            switch (this.setting.getTermCursorStyle()) {
                case 1:
                    this.getTerminalPanel().setCursorShape(CursorShape.STEADY_UNDERLINE);
                    break;
                case 2:
                    this.getTerminalPanel().setCursorShape(CursorShape.STEADY_VERTICAL_BAR);
                    break;
                default:
                    this.getTerminalPanel().setCursorShape(CursorShape.STEADY_BLOCK);
                    break;
            }
        }
    }

    protected String[] getProcessCommand() {
        // 如果设置了指定类型的终端，则直接返回
        String termType = this.setting.getTermType();
        if (StringUtil.isNotBlank(termType)) {
            if (OSUtil.isMacOS()) {
                return new String[]{termType, "--login"};
            }
            if (OSUtil.isWindows()) {
                if ("git-sh".equals(termType)) {
                    return new String[]{"C:\\Program Files\\Git\\bin\\sh.exe", "--login", "-i"};
                }
                if ("git-bash".equals(termType)) {
                    return new String[]{"C:\\Program Files\\Git\\bin\\bash.exe", "--login", "-i"};
                }
                if ("cmd.exe".equals(termType)) {
                    return new String[]{termType, "-l"};
                }
                return new String[]{termType};
            }
            return new String[]{termType};
        }
        String[] command = new String[]{"/bin/bash"};
        Map<String, String> envs = this.getEnvironments();
        if (OSUtil.isWindows()) {
            command = new String[]{"cmd.exe"};
//            command = new String[]{"powershell.exe"};
        } else if (OSUtil.isLinux()) {
            String shell = envs.get("SHELL");
            if (shell == null) {
                shell = "/bin/bash";
            }
            command = new String[]{shell, "-l"};
        } else if (OSUtil.isMacOS()) {
            String shell = envs.get("SHELL");
            if (shell == null) {
                shell = "/bin/bash";
            }
            command = new String[]{shell, "--login"};
        }
        return command;
    }

    protected PtyProcess createProcess() throws IOException {
        Map<String, String> envs = this.getEnvironments();
        String[] command = this.getProcessCommand();
        // this.fixBashEnvironment(envs, command[0]);
        String workingDirectory = Path.of(".").toAbsolutePath().normalize().toString();
        JulLog.info("Starting {} in {}", String.join(" ", command), workingDirectory);
        return new PtyProcessBuilder()
                .setDirectory(workingDirectory)
                .setInitialColumns(80)
                .setInitialRows(24)
                .setCommand(command)
                .setEnvironment(envs)
                .setConsole(false)
                .setUseWinConPty(false)
                // 这个会导致输出混乱，不要为true
                .setRedirectErrorStream(false)
                .setWindowsAnsiColorEnabled(true)
                .start();
    }

    @Override
    public TtyConnector createTtyConnector() throws IOException {
        return this.createTtyConnector(StandardCharsets.UTF_8);
    }

    public TtyConnector createTtyConnector(Charset charset) throws IOException {
        PtyProcess process = this.createProcess();
        String[] command = this.getProcessCommand();
        return new TtyProcessTtyConnector(process, charset, Arrays.asList(command));
    }

    /**
     * 环境列表
     */
    private HashMap<String, String> envs;

    public Map<String, String> getEnvironments() {
        if (this.envs == null) {
            this.envs = new HashMap<>(System.getenv());
            if (OSUtil.isMacOS()) {
                this.envs.put("LC_CTYPE", Charsets.UTF_8.name());
                this.envs.put("LANG", "en_US.utf-8");
                // this.envs.put("TERM", "xterm-256color");
            } else if (OSUtil.isLinux()) {
                this.envs.put("LANG", "en_US.utf-8");
                // this.envs.put("TERM", "xterm-256color");
            } else if (OSUtil.isWindows()) {
                // this.envs.put("TERM", "xterm-256color");
            }
            this.envs.put("TERM", "xterm");
        }
        return this.envs;
    }

    /**
     * 添加环境变量
     *
     * @param key   名称
     * @param value 值
     */
    public void putEnvironment(String key, String value) {
        this.getEnvironments().put(key, value);
    }

    /**
     * 创建zModem协议的tty连接器
     *
     * @param connector tty连接器
     * @return ShellZModemTtyConnector
     */
    public TtyZModemTtyConnector createZModemTtyConnector(TtyProcessTtyConnector connector) {
        return new TtyZModemTtyConnector(this.getTerminal(), connector);
    }
}
