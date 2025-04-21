package cn.oyzh.easyshell.terminal;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.system.OSUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.domain.ShellSetting;
import cn.oyzh.easyshell.store.ShellSettingStore;
import cn.oyzh.jeditermfx.app.pty.TtyConnectorWaitFor;
import com.jediterm.core.util.TermSize;
import com.jediterm.terminal.TtyConnector;
import com.jediterm.terminal.ui.FXJediTermWidget;
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
import java.util.function.IntConsumer;

/**
 * @author oyzh
 * @since 2025-03-04
 */
public class ShellDefaultTermWidget extends FXJediTermWidget {

    /**
     * 设置
     */
    private final ShellSetting setting = ShellSettingStore.SETTING;

    public ShellDefaultTermWidget() {
        super(new ShellSettingsProvider());
    }

    public ShellDefaultTermWidget(SettingsProvider provider) {
        super(provider);
    }

    protected String[] getProcessCommand() {
        // 如果设置了指定类型的终端，则直接返回
        String termType = this.setting.getTermType();
        if (StringUtil.isNotBlank(termType)) {
            if (OSUtil.isMacOS()) {
                return new String[]{termType, "--login"};
            }
//            return new String[]{termType, "-l"};
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

    public TtyConnector createTtyConnector() throws IOException {
        return this.createTtyConnector(StandardCharsets.UTF_8);
    }

    public TtyConnector createTtyConnector(Charset charset) throws IOException {
        PtyProcess process = this.createProcess();
        String[] command = this.getProcessCommand();
        return new ShellDefaultTtyConnector(process, charset, Arrays.asList(command));
    }

    protected Map<String, String> getEnvironments() {
        HashMap<String, String> envs = new HashMap<>(System.getenv());
        if (OSUtil.isMacOS()) {
            envs.put("LC_CTYPE", Charsets.UTF_8.name());
        }
        if (OSUtil.isWindows()) {
            envs.put("TERM", "xterm-256color");
        }
        return envs;
    }

    public void openSession() throws IOException {
        if (this.canOpenSession()) {
            this.openSession(this.createTtyConnector());
        }
    }

    public void openSession(TtyConnector ttyConnector) {
        if (this.canOpenSession()) {
            FXJediTermWidget session = this.createTerminalSession(ttyConnector);
//            if (ttyConnector instanceof ShellDefaultTtyConnector loggingConnector) {
//                loggingConnector.setWidget(session);
//            }
            session.start();
        }
    }

    public TtyConnectorWaitFor onTermination(IntConsumer terminationCallback) {
        return new TtyConnectorWaitFor(this.getTtyConnector(),
                this.getExecutorServiceManager().getUnboundedExecutorService(),
                terminationCallback);
    }

    @Override
    public ShellDefaultTtyConnector getTtyConnector() {
        return (ShellDefaultTtyConnector) super.getTtyConnector();
    }

//    public double getWidth() {
//        return NodeUtil.getWidth(this.getComponent());
//    }
//
//    public double getHeight() {
//        return NodeUtil.getHeight(this.getComponent());
//    }

    public TermSize getTermSize() {
        return this.getTtyConnector().getTermSize();
    }
}
