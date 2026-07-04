package cn.oyzh.easyshell.test.mosh;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.mosh4j.core.MoshTerminalFrontend;

import java.nio.charset.StandardCharsets;

public class MoshFxTerminal extends Application {

    private MoshTerminalFrontend frontend;

    @Override
    public void start(Stage primaryStage) throws Exception {
        TextArea terminal = new TextArea();
        terminal.setStyle(
                "-fx-font-family: 'Courier New'; -fx-font-size: 14;");
        terminal.setEditable(false);
        terminal.setWrapText(false);

        terminal.addEventFilter(KeyEvent.KEY_TYPED, event -> {
            String ch = event.getCharacter();
            if (!ch.isEmpty() && frontend != null) {
                frontend.sendUserInput(
                        ch.getBytes(StandardCharsets.UTF_8));
            }
            event.consume();
        });

        terminal.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            byte[] seq = mapKeyToAnsiSequence(event);
            if (seq != null && frontend != null) {
                frontend.sendUserInput(seq);
            }
        });

        BorderPane root = new BorderPane(terminal);
        Scene scene = new Scene(root, 800, 600);
        primaryStage.setTitle("mosh4j JavaFX Terminal");
        primaryStage.setScene(scene);
        primaryStage.show();

        //InetSocketAddress server = new InetSocketAddress("", 6003);
        //MoshKey key = MoshKey.fromBase64("gZERmM643zmuZgbiX4eHdw");
        //InetSocketAddress server = new InetSocketAddress("192.168.3.21", 60001);
        //MoshKey key = MoshKey.fromBase64("k+ADk2Bjv92Y2R74QwWFqg");
        //MoshKey key = MoshKey.fromBase64("4kYMa9v+P1lOQ0Uy7A==77");
        //MoshClientSession session =
        //        new MoshClientSession(server, key, 80, 24);
        //frontend = new MoshTerminalFrontend(session);
        //frontend.sendInitialWakeUp();
        //frontend.start();

        // 连接参数（请替换成你的实际信息）
        String host = "192.168.3.21";
        int sshPort = 22;
        String username = "root";
        String password = "user@147";  // 或使用私钥（见下文）


        try {
            frontend = MoshAutoConnector.connect(host, sshPort, username, password);
            System.out.println("✅ Mosh 连接成功！");
            // 然后启动渲染线程...
        } catch (Exception e) {
            e.printStackTrace();
            terminal.appendText("❌ 连接失败: " + e.getMessage());
        }

        //frontend.sendUserInput(new byte[]{'\r'});
        //frontend.sendUserInput(new byte[]{'\n'});

        Thread renderThread = new Thread(() -> {
            while (frontend.isRunning()) {
                byte[] hostBytes = frontend.pollHostBytes();
                if (hostBytes != null) {
                    String text = new String(
                            hostBytes, StandardCharsets.UTF_8);
                    Platform.runLater(
                            () -> terminal.appendText(text));
                System.out.println(new String(hostBytes));
                }
            }
        }, "mosh-render");
        renderThread.setDaemon(true);
        renderThread.start();

        scene.widthProperty().addListener((obs, oldV, newV) -> {
            int cols = Math.max(1, newV.intValue() / 9);
            int rows = Math.max(1, (int) scene.getHeight() / 18);
            frontend.sendResize(cols, rows);
        });

        primaryStage.setOnCloseRequest(event -> {
            if (frontend != null) frontend.close();
        });
    }

    private byte[] mapKeyToAnsiSequence(KeyEvent event) {
        return switch (event.getCode()) {
            case ENTER -> new byte[]{'\r'};
            case BACK_SPACE -> new byte[]{0x7f};
            case TAB -> new byte[]{'\t'};
            case ESCAPE -> new byte[]{0x1b};
            case UP -> new byte[]{0x1b, '[', 'A'};
            case DOWN -> new byte[]{0x1b, '[', 'B'};
            case RIGHT -> new byte[]{0x1b, '[', 'C'};
            case LEFT -> new byte[]{0x1b, '[', 'D'};
            case HOME -> new byte[]{0x1b, '[', 'H'};
            case END -> new byte[]{0x1b, '[', 'F'};
            case PAGE_UP -> new byte[]{0x1b, '[', '5', '~'};
            case PAGE_DOWN -> new byte[]{0x1b, '[', '6', '~'};
            case DELETE -> new byte[]{0x1b, '[', '3', '~'};
            case INSERT -> new byte[]{0x1b, '[', '2', '~'};
            default -> null;
        };
    }

    public static void main(String[] args) {
        launch(args);
    }

    public static class MoshFxTerminalStarter {
        static void main(String[] args) {
            MoshFxTerminal.main(args);
        }
    }
}