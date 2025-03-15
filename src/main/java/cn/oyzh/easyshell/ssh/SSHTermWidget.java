package cn.oyzh.easyshell.ssh;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.system.OSUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.domain.ShellSetting;
import cn.oyzh.easyshell.store.SSHSettingStore;
import cn.oyzh.fx.plus.node.NodeUtil;
import com.pty4j.PtyProcess;
import com.pty4j.PtyProcessBuilder;
import com.techsenger.jeditermfx.app.pty.TtyConnectorWaitFor;
import com.techsenger.jeditermfx.core.TtyConnector;
import com.techsenger.jeditermfx.core.util.Platform;
import com.techsenger.jeditermfx.core.util.TermSize;
import com.techsenger.jeditermfx.ui.JediTermFxWidget;
import kotlin.text.Charsets;
import org.jetbrains.annotations.NotNull;

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
public class SSHTermWidget extends JediTermFxWidget {

    /**
     * 设置
     */
    private final ShellSetting setting = SSHSettingStore.SETTING;

    public SSHTermWidget() {
        super(new SSHSettingsProvider());
    }

    protected String[] getProcessCommand() {
        // 如果设置了指定类型的终端，则直接返回
        String terminalType = this.setting.getTerminalType();
        if (StringUtil.isNotBlank(terminalType)) {
            if (OSUtil.isMacOS()) {
                return new String[]{terminalType, "--login"};
            }
            return new String[]{terminalType};
        }
        String[] command = new String[]{"/bin/bash"};
        Map<String, String> envs = this.getEnvironments();
        if (OSUtil.isWindows()) {
//                command = new String[]{"cmd.exe"};
            command = new String[]{"powershell.exe"};
        } else if (OSUtil.isLinux()) {
            String shell = envs.get("SHELL");
            if (shell == null) {
                shell = "/bin/bash";
            }
            command = new String[]{shell};
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
                .setInitialColumns(120)
                .setInitialRows(20)
                .setCommand(command)
                .setEnvironment(envs)
                .setConsole(false)
                .setUseWinConPty(false)
                .setWindowsAnsiColorEnabled(true)
                .start();
    }

    public TtyConnector createTtyConnector() throws IOException {
        return this.createTtyConnector(StandardCharsets.UTF_8);
    }

    public TtyConnector createTtyConnector(Charset charset) throws IOException {
        PtyProcess process = this.createProcess();
        String[] command = this.getProcessCommand();
        return new SSHTtyConnector(process, charset, Arrays.asList(command));
    }

    protected Map<String, String> getEnvironments() {
        HashMap<String, String> envs = new HashMap<>(System.getenv());
        if (Platform.isMacOS()) {
            envs.put("LC_CTYPE", Charsets.UTF_8.name());
        }
        if (Platform.isWindows()) {
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
            JediTermFxWidget session = this.createTerminalSession(ttyConnector);
            if (ttyConnector instanceof SSHTtyConnector loggingConnector) {
                loggingConnector.setWidget(session);
            }
            session.start();
        }
    }

    public void onTermination(@NotNull IntConsumer terminationCallback) {
        new TtyConnectorWaitFor(this.getTtyConnector(),
                this.getExecutorServiceManager().getUnboundedExecutorService(),
                terminationCallback);
    }

    @Override
    public SSHTtyConnector getTtyConnector() {
        return (SSHTtyConnector) super.getTtyConnector();
    }

    public double getWidth() {
        return NodeUtil.getWidth(this.getPane());
    }

    public double getHeight() {
        return NodeUtil.getHeight(this.getPane());
    }

    public TermSize getTermSize() {
        return this.getTtyConnector().getTermSize();
    }
}
