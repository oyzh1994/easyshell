package cn.oyzh.easyshell.test;

import cn.oyzh.easyshell.ssh.ShellSSHClient;
import cn.oyzh.easyshell.ssh.ShellSSHTermWidget;
import cn.oyzh.easyshell.ssh.ShellSSHTtyConnector;
import cn.oyzh.easyshell.terminal.ShellTermWidgetUtil;
import cn.oyzh.fx.plus.controls.box.FXVBox;
import cn.oyzh.fx.plus.util.FXUtil;
import com.jediterm.terminal.TtyConnector;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 验证 SSH 终端原位刷新后的首屏内容。
 */
public class ShellSSHUiRefreshRegression extends Application {

    private ShellSSHClient client;

    private ShellSSHTermWidget widget;

    private Stage stage;

    @Override
    public void start(Stage stage) {
        this.stage = stage;
        FXVBox root = new FXVBox();
        stage.setScene(new Scene(root, 1000, 700));
        stage.setTitle("SSH terminal refresh regression");
        stage.show();
        Thread thread = new Thread(this::runTest, "ssh-ui-refresh-test");
        thread.setDaemon(true);
        thread.start();
    }

    private void runTest() {
        try {
            ShellJSchTestHosts.Host host = ShellJSchTestHosts.load(Integer.getInteger("test.host.index", 6));
            this.client = new ShellSSHClient(host.newConnect());
            this.client.setVerifyFailureCallback(current -> null);
            ShellJSchTestHosts.acceptHostKeys(this.client);
            this.client.start(15000);

            FXUtil.runWait(() -> {
                this.widget = new ShellSSHTermWidget();
                this.widget.setFlexWidth("100%");
                this.widget.setFlexHeight("100%");
                ((FXVBox) this.stage.getScene().getRoot()).getChildren().add(this.widget);
                this.openWidget(this.widget);
            });
            String initial = waitForScreen();
            printScreen("initial", initial);

            ShellSSHTermWidget oldWidget = this.widget;
            ShellSSHTtyConnector oldConnector = oldWidget.getTtyConnector();
            oldConnector.detachClient();
            this.client.reconnect();
            FXUtil.runWait(() -> {
                ShellSSHTermWidget replacement = new ShellSSHTermWidget();
                TtyConnector connector = this.createConnector(replacement);
                ShellTermWidgetUtil.replace(oldWidget, replacement);
                this.widget = replacement;
                replacement.openSession(connector);
                replacement.initPtySize();
                replacement.requestFocus();
            });
            ShellTermWidgetUtil.close(oldWidget);
            String refreshed = waitForScreen();
            printScreen("refreshed", refreshed);
            require(!refreshed.isBlank(), "refreshed terminal screen is empty");
            require(refreshed.contains("$") || refreshed.contains("#")
                            || refreshed.contains("%") || refreshed.contains(">"),
                    "refreshed terminal does not contain a prompt");
            if (Boolean.getBoolean("test.shell.require.banner")) {
                require(containsBanner(refreshed), "refreshed terminal did not render the login banner");
            }

            FXUtil.runWait(() -> {
                ShellTermWidgetUtil.close(this.widget);
                this.stage.close();
            });
            this.client.close();
            System.out.println("ssh ui refresh regression passed");
            Platform.exit();
            System.exit(0);
        } catch (Throwable ex) {
            ex.printStackTrace();
            System.exit(1);
        }
    }

    private TtyConnector openWidget(ShellSSHTermWidget widget) {
        try {
            TtyConnector connector = this.createConnector(widget);
            widget.openSession(connector);
            widget.initPtySize();
            widget.requestFocus();
            return connector;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private TtyConnector createConnector(ShellSSHTermWidget widget) {
        try {
            widget.initBackspaceCode(this.client.getShellConnect().getBackspaceType());
            widget.setAltSendsEscape(this.client.getShellConnect().isAltSendsEscape());
            return widget.createTtyConnector(this.client);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private String waitForScreen() throws Exception {
        String previous = "";
        long stableSince = System.currentTimeMillis();
        long deadline = System.currentTimeMillis() + 15000;
        while (System.currentTimeMillis() < deadline) {
            AtomicReference<String> currentRef = new AtomicReference<>();
            FXUtil.runWait(() -> currentRef.set(String.join("\n", this.widget.getTerminalTextBuffer().getScreenLines())));
            String current = currentRef.get();
            if (!current.equals(previous)) {
                previous = current;
                stableSince = System.currentTimeMillis();
            } else if (System.currentTimeMillis() - stableSince > 800) {
                break;
            }
            Thread.sleep(50);
        }
        return previous;
    }

    private static void printScreen(String name, String screen) {
        System.out.println("--- " + name + " screen ---");
        List<String> lines = screen.lines().toList();
        for (int i = 0; i < lines.size(); i++) {
            System.out.printf("%02d: %s%n", i + 1, lines.get(i));
        }
        System.out.println("--- " + name + " screen end ---");
    }

    private static void require(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }

    private static boolean containsBanner(String output) {
        String value = output.toLowerCase();
        return value.contains("welcome to") || value.contains("system information")
                || value.contains("last login") || value.contains("ubuntu")
                || value.contains("uos") || value.contains("documentation")
                || value.contains("expanded security maintenance") || value.contains("memory usage")
                || value.contains("disk usage") || value.contains("load average");
    }

    public static void main(String[] args) {
        launch(args);
    }
}
