package cn.oyzh.easyssh.test;

import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;

public class SSHTerminalApp2 extends Application {

    private TextArea outputArea;
    private TextField inputField, hostField, userField;
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
            JSch jsch = new JSch();
            session = jsch.getSession(user, host, 22);
            session.setPassword(pass);
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect();

            channel = (ChannelShell) session.openChannel("shell");
            channel.setPty(true);
            channel.setPtySize(1000, 1000, 1000, 1000);
//            channel.setPtyType("xterm");
            channel.connect();

            in = channel.getInputStream();
            out = channel.getOutputStream();

            // 启动线程读取输出
            new Thread(this::readOutput).start();
        } catch (JSchException | IOException e) {
            appendText("Connection error: " + e.getMessage());
        }
    }

    private void readOutput() {
        try {
            byte[] buffer = new byte[4 * 1024];
            while (true) {
                StringBuilder builder = new StringBuilder();
                int len = in.read(buffer);
                if (len <= 0) {
                    continue;
                }
                String data = new String(buffer, 0, len);
                builder.append(data);
                if (!builder.isEmpty()) {
                    String output = builder.toString();
                    Platform.runLater(() -> appendText(output));
                }
            }
        } catch (IOException e) {
            appendText("Read error: " + e.getMessage());
        }
    }

    private void sendCommand() {
        String cmd = inputField.getText() + "\n";
        inputField.clear();
        try {
            out.write(cmd.getBytes());
            out.flush();
        } catch (IOException e) {
            appendText("Send error: " + e.getMessage());
        }
    }

    private void sendCtrlCCommand() {
        inputField.clear();
        try {
            out.write(0x03);
            out.flush();
        } catch (IOException e) {
            appendText("Send error: " + e.getMessage());
        }
    }

    private void sendEscCommand() {
        inputField.clear();
        try {
            out.write(27);
            out.flush();
        } catch (IOException e) {
            appendText("Send error: " + e.getMessage());
        }
    }

    private void appendText(String text) {
        outputArea.appendText(text);
    }

    public void disconnect() {
        if (channel != null) channel.disconnect();
        if (session != null) session.disconnect();
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
        root.getChildren().add(outputArea = new TextArea());
        outputArea.setPrefHeight(800);

        userField.setText("root");
        passField.setText("Oyzh.1994");
        hostField.setText("120.24.176.61");
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.setTitle("SSH Terminal");
        primaryStage.show();

    }

    public static class SSHTerminalApp2Test {

        public static void main(String[] args) throws URISyntaxException {
            SSHTerminalApp2.main(args);
        }

    }


}

