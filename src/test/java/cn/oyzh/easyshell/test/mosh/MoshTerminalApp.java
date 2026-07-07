package cn.oyzh.easyshell.test.mosh;

import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.mosh.ShellMoshClient;
import cn.oyzh.easyshell.mosh.ShellMoshTermWidget;
import cn.oyzh.easyshell.mosh.ShellMoshTtyConnector;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import net.schmizz.sshj.connection.ConnectionException;
import net.schmizz.sshj.transport.TransportException;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;

public class MoshTerminalApp extends Application {

    private ShellMoshTermWidget widget = new ShellMoshTermWidget();
    private TextField inputField, hostField, userField;
    private TextField passField;


    private void connect() {
        String host = hostField.getText();
        String user = userField.getText();
        String pass = passField.getText();

        // System.setProperty(org.slf4j.impl.SimpleLogger.DEFAULT_LOG_LEVEL_KEY, "DEBUG");

        try {

            ShellConnect connect = new ShellConnect();
            connect.setUser(user);
            connect.setPassword(pass);
            connect.setHost(host + ":22");
            ShellMoshClient client = new ShellMoshClient(connect);
            client.start();
            if(!client.isConnected()){
                return;
            }

            ShellMoshTtyConnector connector = widget.createTtyConnector(Charset.defaultCharset());
            connector.init(client);
            this.widget.openSession(connector);

//            // 使用 JediTermFX 实际终端尺寸（基于字体度量），而非像素估算
//            connector.terminalSizeProperty().addListener((obs, oldV, newV) -> {
//                if (newV != null) {
//                    client.getFrontend().sendResize(newV.getColumns(), newV.getRows());
//                }
//            });

//            ShellZModemTtyConnector adaptor = new ShellZModemTtyConnector(widget.getTerminal(), connector);
//            this.widget.openSession(adaptor);
//
//            widget.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
//                byte[] seq = mapKeyToAnsiSequence(event);
//                if (seq != null && frontend != null) {
//                    frontend.sendUserInput(seq);
//                }
//            });


        } catch (Throwable e) {
            e.printStackTrace();
        }
    }


//    private byte[] mapKeyToAnsiSequence(KeyEvent event) {
//        return switch (event.getCode()) {
//            //case ENTER -> new byte[]{'\r'};
//            case BACK_SPACE -> new byte[]{0x7f};
//            case TAB -> new byte[]{'\t'};
//            case ESCAPE -> new byte[]{0x1b};
//            case UP -> new byte[]{0x1b, '[', 'A'};
//            case DOWN -> new byte[]{0x1b, '[', 'B'};
//            case RIGHT -> new byte[]{0x1b, '[', 'C'};
//            case LEFT -> new byte[]{0x1b, '[', 'D'};
//            case HOME -> new byte[]{0x1b, '[', 'H'};
//            case END -> new byte[]{0x1b, '[', 'F'};
//            case PAGE_UP -> new byte[]{0x1b, '[', '5', '~'};
//            case PAGE_DOWN -> new byte[]{0x1b, '[', '6', '~'};
//            case DELETE -> new byte[]{0x1b, '[', '3', '~'};
//            case INSERT -> new byte[]{0x1b, '[', '2', '~'};
//            default -> null;
//        };
//    }

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
        passField.setText("oyzh@2026");
        hostField.setText("120.24.176.61");
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Mosh Terminal");
        primaryStage.show();
    }

    public static class MoshTerminalApTest {

        public static void main(String[] args) throws URISyntaxException {
            MoshTerminalApp.main(args);
        }

    }


}

