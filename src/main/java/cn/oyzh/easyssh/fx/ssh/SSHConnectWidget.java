package cn.oyzh.easyssh.fx.ssh;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.system.OSUtil;
import cn.oyzh.fx.plus.node.NodeUtil;
import com.pty4j.PtyProcess;
import com.pty4j.PtyProcessBuilder;
import com.techsenger.jeditermfx.app.pty.TtyConnectorWaitFor;
import com.techsenger.jeditermfx.core.TtyConnector;
import com.techsenger.jeditermfx.core.util.Platform;
import com.techsenger.jeditermfx.core.util.TermSize;
import com.techsenger.jeditermfx.ui.JediTermFxWidget;
import com.techsenger.jeditermfx.ui.settings.SettingsProvider;
import kotlin.text.Charsets;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

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
public class SSHConnectWidget extends JediTermFxWidget {

    @Getter
    private PtyProcess process;

    public SSHConnectWidget(@NotNull SettingsProvider settingsProvider) {
        super(settingsProvider);
    }

    public SSHConnectWidget(int columns, int lines, SettingsProvider settingsProvider) {
        super(columns, lines, settingsProvider);
    }

    public TtyConnector createTtyConnector() {
        try {
            var envs = this.configureEnvironmentVariables();
            String[] command;
            if (OSUtil.isWindows()) {
//                command = new String[]{"cmd.exe"};
                command = new String[]{"powershell.exe"};
            } else if (OSUtil.isLinux()) {
                String shell = envs.get("SHELL");
                if (shell == null) {
                    shell = "/bin/bash";
                }
                command = new String[]{shell};
            } else {
                String shell = envs.get("SHELL");
                if (shell == null) {
                    shell = "/bin/bash";
                }
                command = new String[]{shell, "--login"};
            }
            var workingDirectory = Path.of(".").toAbsolutePath().normalize().toString();
            JulLog.info("Starting {} in {}", String.join(" ", command), workingDirectory);
            this.process = new PtyProcessBuilder()
                    .setDirectory(workingDirectory)
                    .setInitialColumns(120)
                    .setInitialRows(20)
                    .setCommand(command)
                    .setEnvironment(envs)
                    .setConsole(false)
//                    .setUseWinConPty(true)
                    .setWindowsAnsiColorEnabled(true)
                    .start();
            return new SSHTtyConnector(this.process, StandardCharsets.UTF_8, Arrays.asList(command));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private Map<String, String> configureEnvironmentVariables() {
        HashMap<String, String> envs = new HashMap<>(System.getenv());
        if (Platform.isMacOS()) {
            envs.put("LC_CTYPE", Charsets.UTF_8.name());
        }
        if (Platform.isWindows()) {
            envs.put("TERM", "xterm-256color");
        }
        return envs;
    }

    public void openSession() {
        if (this.canOpenSession()) {
            openSession(this.createTtyConnector());
        }
    }

    public void openSession(TtyConnector ttyConnector) {
        JediTermFxWidget session = this.createTerminalSession(ttyConnector);
        if (ttyConnector instanceof SSHTtyConnector loggingConnector) {
            loggingConnector.setWidget(session);
        }
        session.start();
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
