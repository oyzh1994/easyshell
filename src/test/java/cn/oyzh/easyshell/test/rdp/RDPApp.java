package cn.oyzh.easyshell.test.rdp;

import cn.oyzh.easyshell.domain.ShellConnect;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.net.URISyntaxException;

public class RDPApp extends Application {

    private Canvas widget = new Canvas();
    private TextField inputField, hostField, userField;
    private TextField passField;

    private NativeRDPClient rdpClient;

    private void connect() {
        String host = hostField.getText();
        String user = userField.getText();
        String pass = passField.getText();

        // System.setProperty(org.slf4j.impl.SimpleLogger.DEFAULT_LOG_LEVEL_KEY, "DEBUG");

        try {
            ShellConnect connect = new ShellConnect();
            connect.setHost(host);
            connect.setUser(user);
            connect.setPassword(pass);
            rdpClient = new NativeRDPClient(connect, widget);

            rdpClient.start(1500);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public void disconnect() throws Exception {
        rdpClient.close();
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
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        HBox box = new HBox(button, button1);
        root.getChildren().add(box);
        root.getChildren().add(inputField = new TextField());


        root.getChildren().add(widget);
        widget.setHeight(600);
        widget.setWidth(800);

        userField.setText("oyzh");
        passField.setText("123456");
        hostField.setText("192.168.3.156:3389");
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.setTitle("RDP Demo");
        primaryStage.show();

    }

    public static class RDPAppTest {

        public static void main(String[] args) throws URISyntaxException {
            RDPApp.main(args);
        }

    }


}
