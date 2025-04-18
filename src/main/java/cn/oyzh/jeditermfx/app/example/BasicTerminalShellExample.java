package cn.oyzh.jeditermfx.app.example;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.jeditermfx.terminal.ui.DefaultHyperlinkFilter;
import com.jediterm.core.Platform;
import com.jediterm.terminal.TtyConnector;
import com.pty4j.PtyProcess;
import com.pty4j.PtyProcessBuilder;
import cn.oyzh.jeditermfx.app.pty.PtyProcessTtyConnector;
import com.jediterm.terminal.ui.JediTermFxWidget;
import cn.oyzh.jeditermfx.terminal.ui.settings.DefaultSettingsProvider;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class BasicTerminalShellExample extends Application {

    private @NotNull JediTermFxWidget createTerminalWidget() {
        JediTermFxWidget widget = new JediTermFxWidget(80, 24, new DefaultSettingsProvider());
        widget.setTtyConnector(createTtyConnector());
        widget.addHyperlinkFilter(new DefaultHyperlinkFilter());
        widget.start();
        return widget;
    }

    private @NotNull TtyConnector createTtyConnector() {
        try {
            Map<String, String> envs = System.getenv();
            String[] command;
            if (Platform.isWindows()) {
                command = new String[]{"cmd.exe"};
            } else {
                command = new String[]{"/bin/bash", "--login"};
                envs = new HashMap<>(System.getenv());
                envs.put("TERM", "xterm-256color");
            }
            PtyProcess process = new PtyProcessBuilder().setCommand(command).setEnvironment(envs).start();
            return new PtyProcessTtyConnector(process, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void start(Stage stage) {
        JediTermFxWidget widget = createTerminalWidget();
        widget.addListener(terminalWidget -> {
            widget.close(); // terminate the current process and dispose all allocated resources
            JulLog.debug("Closed widget");
        });
        stage.setTitle("Basic Terminal Shell Example");
        stage.setOnCloseRequest(e -> {
            widget.close();
            widget.getTtyConnector().close(); // terminate the current process
            JulLog.debug("Closed TTY connector");
        });
        Scene scene = new Scene(widget.getPane(), 600, 400);
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
