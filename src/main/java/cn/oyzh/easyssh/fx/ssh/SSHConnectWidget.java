package cn.oyzh.easyssh.fx.ssh;

import cn.oyzh.common.log.JulLog;
import com.pty4j.PtyProcess;
import com.pty4j.PtyProcessBuilder;
import com.techsenger.jeditermfx.app.JediTermFx;
import com.techsenger.jeditermfx.app.debug.TerminalDebugUtil;
import com.techsenger.jeditermfx.app.pty.LoggingTtyConnector;
import com.techsenger.jeditermfx.app.pty.PtyProcessTtyConnector;
import com.techsenger.jeditermfx.app.pty.TtyConnectorWaitFor;
import com.techsenger.jeditermfx.core.TtyConnector;
import com.techsenger.jeditermfx.core.model.TerminalTextBuffer;
import com.techsenger.jeditermfx.core.util.Platform;
import com.techsenger.jeditermfx.ui.DefaultHyperlinkFilter;
import com.techsenger.jeditermfx.ui.JediTermFxWidget;
import com.techsenger.jeditermfx.ui.TerminalWidget;
import com.techsenger.jeditermfx.ui.settings.SettingsProvider;
import kotlin.collections.ArraysKt;
import kotlin.jvm.internal.Intrinsics;
import kotlin.text.Charsets;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.IntConsumer;

/**
 * @author oyzh
 * @since 2025-03-04
 */
public class SSHConnectWidget extends JediTermFxWidget {

    public SSHConnectWidget(@NotNull SettingsProvider settingsProvider) {
        super(settingsProvider);
    }

    public SSHConnectWidget(int columns, int lines, SettingsProvider settingsProvider) {
        super(columns, lines, settingsProvider);
    }

    public TtyConnector createTtyConnector() {
        try {
            var envs = configureEnvironmentVariables();
            String[] command;
            if (Platform.isWindows()) {
//                command = new String[]{"cmd.exe"};
                command = new String[]{"powershell.exe"};
            } else {
                String shell = (String) envs.get("SHELL");
                if (shell == null) {
                    shell = "/bin/bash";
                }
                if (Platform.isMacOS()) {
                    command = new String[]{shell, "--login"};
                } else {
                    command = new String[]{shell};
                }
            }
            var workingDirectory = Path.of(".").toAbsolutePath().normalize().toString();
            JulLog.info("Starting {} in {}", String.join(" ", command), workingDirectory);
            var process = new PtyProcessBuilder()
                    .setDirectory(workingDirectory)
                    .setInitialColumns(120)
                    .setInitialRows(20)
                    .setCommand(command)
                    .setEnvironment(envs)
                    .setConsole(false)
                    .setUseWinConPty(true)
                    .start();
            return new JediTermFx.LoggingPtyProcessTtyConnector(process, StandardCharsets.UTF_8, Arrays.asList(command));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private final Map<String, String> configureEnvironmentVariables() {
        HashMap envs = new HashMap<String, String>(System.getenv());
        if (Platform.isMacOS()) {
            envs.put("LC_CTYPE", Charsets.UTF_8.name());
        }
        if (!Platform.isWindows()) {
            envs.put("TERM", "xterm-256color");
        }
        return envs;
    }

    @NotNull
    protected JediTermFxWidget createTerminalWidget(@NotNull SettingsProvider settingsProvider) {
        Intrinsics.checkNotNullParameter(settingsProvider, "settingsProvider");
        JediTermFxWidget widget = new JediTermFxWidget(settingsProvider);
        widget.addHyperlinkFilter(new DefaultHyperlinkFilter());
        return widget;
    }

    public void openSession( ) {
        if (this.canOpenSession()) {
            openSession(createTtyConnector());
        }
    }

    public void openSession( TtyConnector ttyConnector) {
        JediTermFxWidget session = this.createTerminalSession(ttyConnector);
        if (ttyConnector instanceof JediTermFx.LoggingPtyProcessTtyConnector) {
            ((JediTermFx.LoggingPtyProcessTtyConnector) ttyConnector).setWidget(session);
        }
        session.start();
    }

    public  void onTermination( @NotNull IntConsumer terminationCallback) {
        new TtyConnectorWaitFor(this.getTtyConnector(),
                this.getExecutorServiceManager().getUnboundedExecutorService(),
                terminationCallback);
    }
}
