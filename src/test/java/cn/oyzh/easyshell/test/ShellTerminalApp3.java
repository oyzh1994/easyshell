package cn.oyzh.easyshell.test;

import cn.oyzh.easyshell.zmodem.ZModemPtyConnectorAdaptor;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.connection.ConnectionException;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.transport.TransportException;
import net.schmizz.sshj.transport.verification.HostKeyVerifier;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.security.PublicKey;
import java.util.List;

public class ShellTerminalApp3 extends Application {

    private ShellTestTermWidget widget = new ShellTestTermWidget();
    private TextField inputField, hostField, userField;
    private TextField passField;

    private Session session;

    private Session.Shell channel;

    //private InputStream in;
    //
    private OutputStream out;

    private void connect() {
        String host = hostField.getText();
        String user = userField.getText();
        String pass = passField.getText();

        try {
            SSHClient ssh = new SSHClient();
            // 禁用主机密钥验证（不推荐在生产环境使用）
            ssh.addHostKeyVerifier(new HostKeyVerifier() {
                @Override
                public boolean verify(String hostname, int port, PublicKey key) {
                    return true; // 始终信任
                }

                @Override
                public List<String> findExistingAlgorithms(String hostname, int port) {
                    return List.of();
                }
            });

            ssh.connect(host, 22);
            ssh.authPassword(user, pass);

            session = ssh.startSession();

            session.allocateDefaultPTY();


            channel = session.startShell();


            //in = channel.getInputStream();
            out = channel.getOutputStream();

            ShellTestTtyConnector connector=  widget.createTtyConnector(Charset.defaultCharset());
            connector.init(channel);

            ZModemPtyConnectorAdaptor adaptor=new ZModemPtyConnectorAdaptor(widget.getTerminal(),connector);

            this.widget.openSession(adaptor);
            //this.widget.openSession(connector);

            //// 启动线程读取输出
            //new Thread(this::readOutput).start();
        } catch (IOException e) {
            try {
                appendText("Connection error: " + e.getMessage());
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    //private void readOutput() {
    //    try {
    //        byte[] buffer = new byte[4 * 1024];
    //        while (true) {
    //            StringBuilder builder = new StringBuilder();
    //            int len = in.read(buffer);
    //            if (len <= 0) {
    //                continue;
    //            }
    //            String data = new String(buffer, 0, len);
    //            builder.append(data);
    //            if (!builder.isEmpty()) {
    //                String output = builder.toString();
    //                Platform.runLater(() -> {
    //                    try {
    //                        appendText(output);
    //                    } catch (IOException e) {
    //                        throw new RuntimeException(e);
    //                    }
    //                });
    //            }
    //        }
    //    } catch (IOException e) {
    //        try {
    //            appendText("Read error: " + e.getMessage());
    //        } catch (IOException ex) {
    //            throw new RuntimeException(ex);
    //        }
    //    }
    //}
    //
    //private void sendCommand() {
    //    String cmd = inputField.getText() + "\n";
    //    inputField.clear();
    //    try {
    //        out.write(cmd.getBytes());
    //        out.flush();
    //    } catch (IOException e) {
    //        throw new RuntimeException(e);
    //    }
    //}

    private void sendCtrlCCommand() {
        inputField.clear();
        try {
            out.write(0x03);
            out.flush();
        } catch (IOException e) {
        }
    }

    private void sendEscCommand() {
        inputField.clear();
        try {
            out.write(27);
            out.flush();
        } catch (IOException e) {
        }
    }

    private void appendText(String text) throws IOException {
        widget.getTtyConnector().write(text);
    }

    public void disconnect() throws TransportException, ConnectionException {
        if (channel != null) channel.close();
        if (session != null) session.close();
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
            }
        });
        HBox box = new HBox(button, button1);
        root.getChildren().add(box);
        root.getChildren().add(inputField = new TextField());
        //Button button2 = new Button("发送");
        //button2.setOnAction(event -> {
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
        HBox box1 = new HBox( button3, button4);
        root.getChildren().add(box1);


        root.getChildren().add(widget);
        widget.setPrefHeight(600);
        widget.setPrefWidth(800);

        userField.setText("root");
        passField.setText("");
        hostField.setText("120.24.176.61");
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.setTitle("SSH Terminal");
        primaryStage.show();

    }

    public static class SSHTerminalApp3Test {

        public static void main(String[] args) throws URISyntaxException {
            ShellTerminalApp3.main(args);
        }

    }


}

