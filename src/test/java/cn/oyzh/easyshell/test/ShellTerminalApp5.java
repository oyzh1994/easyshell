package cn.oyzh.easyshell.test;

import cn.oyzh.common.thread.ThreadUtil;
import cn.oyzh.easyshell.test.mosh.MoshAutoConnector;
import cn.oyzh.easyshell.zmodem.ShellZModemTtyConnector;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
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

            //frontend.sendUserInput(new byte[]{'\r'});
            //frontend.sendUserInput(new byte[]{'\n'});

            // Render thread: 驱动 UDP 接收 + 消费渲染帧
            Thread renderThread = new Thread(() -> {
                //int idleCount = 0;
                while (frontend.isRunning()) {
                    //boolean progressed = frontend.pollOnce();
                    byte[] bytes= frontend.pollHostBytes();
                    if (bytes != null) {
                        try {
                            hostOutputPipe.write(bytes);
                            hostOutputPipe.flush();
                        } catch (IOException e) {
                            break;
                        }
                    //    idleCount = 0;
                    //} else if (progressed) {
                    //    idleCount = 0;
                    //} else {
                    //    idleCount++;
                    //    if (idleCount > 100) {
                    //        ThreadUtil.sleep(20);
                    //        idleCount = 0;
                    //    }
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
                        }
                        ThreadUtil.sleep(40);
                    } catch (IOException e) {
                        break;
                    }
                }
            }, "mosh-input");
            inputThread.setDaemon(true);
            inputThread.start();

            ShellTestTtyConnector connector = widget.createTtyConnector(Charset.defaultCharset());
            // init(OutputStream out, InputStream in)
            //   out → terminal writes keystrokes here → piped to Mosh frontend
            //   in  → terminal reads host output from here → piped from render thread
            connector.init(keyOutputPipe, hostInputPipe);
            ShellZModemTtyConnector adaptor = new ShellZModemTtyConnector(widget.getTerminal(), connector);
            this.widget.openSession(adaptor);
        } catch (Exception e) {
            e.printStackTrace();
        }
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
        passField.setText("user@147");
        hostField.setText("192.168.3.21");
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

