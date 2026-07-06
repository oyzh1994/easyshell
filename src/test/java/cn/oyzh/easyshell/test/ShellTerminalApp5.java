package cn.oyzh.easyshell.test;

import cn.oyzh.common.thread.ThreadUtil;
import cn.oyzh.easyshell.test.mosh.MoshAutoConnector;
import cn.oyzh.fx.tty.zmodem.TtyZModemTtyConnector;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import net.schmizz.sshj.connection.ConnectionException;
import net.schmizz.sshj.transport.TransportException;
import org.mosh4j.core.MoshTerminalFrontend;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.net.URISyntaxException;
import java.nio.charset.Charset;

public class ShellTerminalApp5 extends Application {

    private ShellTestTermWidget widget = new ShellTestTermWidget();
    private TextField inputField, hostField, userField;
    private TextField passField;


    private void connect() {
        String host = hostField.getText();
        String user = userField.getText();
        String pass = passField.getText();

        // System.setProperty(org.slf4j.impl.SimpleLogger.DEFAULT_LOG_LEVEL_KEY, "DEBUG");

        try {
            MoshTerminalFrontend frontend = MoshAutoConnector.connect(host, 22, user, pass);
            final int pipeCapacity = 65536;

            // Pipe: render thread → terminal display
            PipedOutputStream hostOutputPipe = new PipedOutputStream();
            PipedInputStream hostInputPipe = new PipedInputStream(hostOutputPipe, pipeCapacity);

            // Pipe: terminal keystrokes → Mosh frontend
            PipedOutputStream keyOutputPipe = new PipedOutputStream();
            PipedInputStream keyInputPipe = new PipedInputStream(keyOutputPipe, pipeCapacity);

            // Render thread: 驱动 UDP 接收 + 消费 StatefulAnsiRenderer 渲染帧（含颜色）
            Thread renderThread = new Thread(() -> {
               // int idleCount = 0;
                while (frontend.isRunning()) {
                    //boolean progressed = frontend.pollOnce();
                    byte[] bytes = frontend.pollHostBytes();
                    if (bytes != null ) {
                        try {
                            hostOutputPipe.write(bytes);
                            hostOutputPipe.flush();
                        } catch (IOException e) {
                            break;
                        }
                    //    idleCount = 0;
                    //} else if (progressed) {
                    //    idleCount = 0;
                    } else {
                        //idleCount++;
                        //if (idleCount > 100) {
                            ThreadUtil.sleep(40);
                            //idleCount = 0;
                        //}
                    }
                }
            }, "mosh-render");
            renderThread.setDaemon(true);
            renderThread.start();

            // Input thread: read terminal keyboard input → send to Mosh frontend
            Thread inputThread = new Thread(() -> {
                byte[] buffer = new byte[4096];
                while (frontend.isRunning()) {
                    try {
                        int len = keyInputPipe.read(buffer);
                        if (len > 0) {
                            byte[] data = new byte[len];
                            System.arraycopy(buffer, 0, data, 0, len);
                            frontend.sendUserInput(data);
                        }else {
                            ThreadUtil.sleep(40);
                        }
                    } catch (IOException e) {
                        break;
                    }
                }
            }, "mosh-input");
            inputThread.setDaemon(true);
            inputThread.start();

            ShellTestTtyConnector connector = widget.createTtyConnector(Charset.defaultCharset());
            connector.init(keyOutputPipe, hostInputPipe);

            // 使用 JediTermFX 实际终端尺寸（基于字体度量），而非像素估算
            connector.terminalSizeProperty().addListener((obs, oldV, newV) -> {
                if (newV != null) {
                    frontend.sendResize(newV.getColumns(), newV.getRows());
                }
            });

            TtyZModemTtyConnector adaptor = new TtyZModemTtyConnector(widget.getTerminal(), connector);
            this.widget.openSession(adaptor);

            widget.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
                byte[] seq = mapKeyToAnsiSequence(event);
                if (seq != null && frontend != null) {
                    frontend.sendUserInput(seq);
                }
            });


        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private byte[] mapKeyToAnsiSequence(KeyEvent event) {
        return switch (event.getCode()) {
            //case ENTER -> new byte[]{'\r'};
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

    private void sendCtrlCCommand() {
        inputField.clear();
        //try {
        //    out.write(0x03);
        //    out.flush();
        //} catch (IOException e) {
        //}
    }

    private void sendEscCommand() {
        inputField.clear();
        //try {
        //    out.write(27);
        //    out.flush();
        //} catch (IOException e) {
        //}
    }

    private void appendText(String text) throws IOException {
        widget.getTtyConnector().write(text);
    }

    public void disconnect() throws IOException {
    }

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {

        VBox root = new VBox();
        root.setSpacing(10);
        root.getChildren().add(hostField = new TextField());
        root.getChildren().add(userField = new TextField());
        root.getChildren().add(passField = new TextField());

        Button button = new Button("连接");
        button.setOnAction(event -> {
            connect();
        });
        Button button1 = new Button("断开");
        button1.setOnAction(event -> {
            try {
                disconnect();
            } catch (TransportException e) {
                throw new RuntimeException(e);
            } catch (ConnectionException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        HBox box = new HBox(button, button1);
        root.getChildren().add(box);
        root.getChildren().add(inputField = new TextField());
        // Button button2 = new Button("发送");
        // button2.setOnAction(event -> {
        //    sendCommand();
        //});
        Button button3 = new Button("中断");
        button3.setOnAction(event -> {
            sendCtrlCCommand();
        });
        Button button4 = new Button("esc");
        button4.setOnAction(event -> {
            sendEscCommand();
        });
        HBox box1 = new HBox(button3, button4);
        root.getChildren().add(box1);


        root.getChildren().add(widget);
        widget.setPrefHeight(600);
        widget.setPrefWidth(800);

        userField.setText("root");
        passField.setText("");
        hostField.setText("");
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.setTitle("SSH Terminal");
        primaryStage.show();
    }

    public static class SSHTerminalAp5Test {

        public static void main(String[] args) throws URISyntaxException {
            ShellTerminalApp5.main(args);
        }

    }


}

