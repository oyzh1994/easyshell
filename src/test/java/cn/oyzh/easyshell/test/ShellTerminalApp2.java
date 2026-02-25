package cn.oyzh.easyshell.test;

import cn.oyzh.easyshell.zmodem.ShellZModemTtyConnector;
import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.nio.charset.Charset;

public class ShellTerminalApp2 extends Application {

    private ShellTestTermWidget widget;
    private TextField inputField, hostField, userField;
    private VBox root;
    private TextField passField;

    private Session session;

    private ChannelShell channel;

    private InputStream in;

    private OutputStream out;

    private void connect() {
        String host = hostField.getText();
        String user = userField.getText();
        String pass = passField.getText();
        try {
            this.initWidget(user, host, pass);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void initWidget(String user, String host, String pass) throws JSchException, IOException {
        JSch jsch = new JSch();
        widget = new ShellTestTermWidget();
        root.getChildren().add(widget);
        widget.setPrefHeight(600);
        widget.setPrefWidth(800);
        session = jsch.getSession(user, host, 22);
        session.setPassword(pass);
        session.setConfig("StrictHostKeyChecking", "no");
        // session.setConfig("max_input_buffer_size", (64 * 1024) + "");
        session.connect();

        channel = (ChannelShell) session.openChannel("shell");
        channel.setInputStream(System.in);
        channel.setOutputStream(System.out);
        channel.setPty(true);
        channel.setPtyType("xterm");

        // channel.setPtySize(80, 25, 600, 600);

        in = channel.getInputStream();
        out = channel.getOutputStream();

        ShellTestTtyConnector connector = widget.createTtyConnector(Charset.defaultCharset());
        connector.init(channel);
        // connector.setReset(()->FXUtil.runLater(this::connect));
        ShellZModemTtyConnector adaptor = new ShellZModemTtyConnector(widget.getTerminal(), connector);
        this.widget.openSession(adaptor);
        channel.connect();
        // out.write(("stty -ixon -ixoff\n").getBytes());

        InputStream err = channel.getExtInputStream();

        new Thread(() -> {
            try {
                byte[] errBuffer = new byte[1024];
                int errBytesRead;
                while ((errBytesRead = err.read(errBuffer)) != -1) {
                    System.err.print(new String(errBuffer, 0, errBytesRead));
                }
                System.err.println("--------------1");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }).start();
    }

    private void sendCommand() {
        String cmd = inputField.getText() + "\n";
        inputField.clear();
        try {
            out.write(cmd.getBytes());
            out.flush();
        } catch (IOException e) {
            // appendText("Send error: " + e.getMessage());
        }
    }

    private void sendCtrlCCommand() {
        inputField.clear();
        try {
            out.write(0x03);
            out.flush();
        } catch (IOException e) {
            // appendText("Send error: " + e.getMessage());
        }
    }

    private void sendEscCommand() {
        inputField.clear();
        try {
            out.write(27);
            out.flush();
        } catch (IOException e) {
            // appendText("Send error: " + e.getMessage());
        }
    }

    // private void appendText(String text) {
    //    outputArea.appendText(text);
    //}

    public void disconnect() {
        if (channel != null) {
            new Thread(() -> {
                channel.disconnect();
            }).start();
        }
        if (session != null) {
            new Thread(() -> {
                session.disconnect();
            }).start();
        }
        if (widget != null) {
            new Thread(() -> {
                widget.close();
            }).start();
            root.getChildren().remove(widget);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {

        root = new VBox();
        root.setSpacing(10);
        root.setPrefWidth(800);
        root.setPrefHeight(800);
        root.getChildren().add(hostField = new TextField());
        root.getChildren().add(userField = new TextField());
        root.getChildren().add(passField = new TextField());

        Button button = new Button("连接");
        button.setOnAction(event -> {
            connect();
        });
        Button button1 = new Button("断开");
        button1.setOnAction(event -> {
            disconnect();
        });
        HBox box = new HBox(button, button1);
        root.getChildren().add(box);
        root.getChildren().add(inputField = new TextField());
        Button button2 = new Button("发送");
        button2.setOnAction(event -> {
            sendCommand();
        });
        Button button3 = new Button("中断");
        button3.setOnAction(event -> {
            sendCtrlCCommand();
        });
        Button button4 = new Button("esc");
        button4.setOnAction(event -> {
            sendEscCommand();
        });
        HBox box1 = new HBox(button2, button3, button4);
        root.getChildren().add(box1);
        // root.getChildren().add(widget);
        // widget.setPrefHeight(600);
        // widget.setPrefWidth(800);

        userField.setText("root");
        passField.setText("123456");
        hostField.setText("127.0.0.1");
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.setTitle("SSH Terminal");
        primaryStage.show();

    }

    public static class SSHTerminalApp2Test {

        public static void main(String[] args) throws URISyntaxException {
            ShellTerminalApp2.main(args);
        }
    }
}

