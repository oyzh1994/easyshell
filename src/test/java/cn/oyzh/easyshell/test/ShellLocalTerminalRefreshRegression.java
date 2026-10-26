package cn.oyzh.easyshell.test;

import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.local.ShellLocalClient;
import cn.oyzh.easyshell.local.ShellLocalTermWidget;
import cn.oyzh.easyshell.local.ShellLocalTtyConnector;
import cn.oyzh.easyshell.terminal.ShellTermWidgetUtil;
import cn.oyzh.fx.plus.util.FXUtil;
import com.jediterm.terminal.TtyConnector;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 验证非 SSH 终端也能原位替换，并且新终端会话可以正常渲染命令输出。
 */
public class ShellLocalTerminalRefreshRegression extends Application {

    private ShellLocalTermWidget widget;

    private ShellConnect connect;

    private Stage stage;

    @Override
    public void start(Stage stage) {
        this.stage = stage;
        VBox root = new VBox();
        this.connect = new ShellConnect();
        this.connect.setType("local");
        this.connect.setCharset(StandardCharsets.UTF_8.name());
        this.connect.setTermType("xterm-256color");
        this.widget = new ShellLocalTermWidget();
        this.widget.setFlexWidth("100%");
        this.widget.setFlexHeight("100% - 30");
        root.getChildren().add(this.widget);
        stage.setScene(new Scene(root, 760, 520));
        stage.show();

        AtomicReference<TtyConnector> connectorRef = new AtomicReference<>();
        FXUtil.runWait(() -> connectorRef.set(this.initWidget(this.widget, new ShellLocalClient(this.connect))));
        Thread testThread = new Thread(() -> this.runTest(connectorRef.get()), "local-terminal-refresh-test");
        testThread.setDaemon(true);
        testThread.start();
    }

    private void runTest(TtyConnector oldConnector) {
        try {
            String oldMarker = "__LOCAL_REFRESH_OLD_" + UUID.randomUUID().toString().replace("-", "") + "__";
            oldConnector.write("printf '" + oldMarker + "\\n'\r");
            waitForText(this.widget, oldMarker);

            ShellLocalTermWidget oldWidget = this.widget;
            ShellLocalClient newClient = new ShellLocalClient(this.connect);
            AtomicReference<TtyConnector> newConnectorRef = new AtomicReference<>();
            FXUtil.runWait(() -> {
                ShellLocalTermWidget replacement = new ShellLocalTermWidget();
                newConnectorRef.set(this.initWidget(replacement, newClient));
                ShellTermWidgetUtil.replace(oldWidget, replacement);
                this.widget = replacement;
            });
            ShellTermWidgetUtil.close(oldWidget);

            String newMarker = "__LOCAL_REFRESH_NEW_" + UUID.randomUUID().toString().replace("-", "") + "__";
            newConnectorRef.get().write("printf '" + newMarker + "\\n'\r");
            waitForText(this.widget, newMarker);

            ShellLocalTermWidget current = this.widget;
            FXUtil.runWait(() -> {
                ShellTermWidgetUtil.close(current);
                this.stage.close();
            });
            System.out.println("local terminal refresh regression passed");
            System.exit(0);
        } catch (Throwable ex) {
            ex.printStackTrace();
            System.exit(1);
        }
    }

    private TtyConnector initWidget(ShellLocalTermWidget widget, ShellLocalClient client) {
        try {
            ShellLocalTtyConnector connector = widget.createTtyConnector(client);
            widget.initBackspaceCode(this.connect.getBackspaceType());
            widget.setAltSendsEscape(this.connect.isAltSendsEscape());
            widget.openSession(connector);
            return connector;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private static void waitForText(ShellLocalTermWidget widget, String marker) throws Exception {
        long deadline = System.currentTimeMillis() + 10000;
        while (System.currentTimeMillis() < deadline) {
            if (widget.getTerminalTextBuffer().getScreenLines().contains(marker)) {
                return;
            }
            Thread.sleep(50);
        }
        throw new IllegalStateException("terminal output was not rendered: " + marker);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
