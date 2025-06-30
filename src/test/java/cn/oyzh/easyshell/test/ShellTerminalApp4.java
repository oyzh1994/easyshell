package cn.oyzh.easyshell.test;

import cn.oyzh.easyshell.zmodem.ZModemTtyConnector;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import net.schmizz.sshj.connection.ConnectionException;
import net.schmizz.sshj.transport.TransportException;
import org.apache.sshd.client.SshClient;
import org.apache.sshd.client.channel.ChannelShell;
import org.apache.sshd.client.future.ConnectFuture;
import org.apache.sshd.client.keyverifier.AcceptAllServerKeyVerifier;
import org.apache.sshd.client.session.ClientSession;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.nio.charset.Charset;

public class ShellTerminalApp4 extends Application {

   private ShellTestTermWidget widget = new ShellTestTermWidget();
   private TextField inputField, hostField, userField;
   private TextField passField;

private ClientSession session;
   private ChannelShell channel;

   // private InputStream in;
   //
   private OutputStream out;

   private void connect() {
       String host = hostField.getText();
       String user = userField.getText();
       String pass = passField.getText();

       // System.setProperty(org.slf4j.impl.SimpleLogger.DEFAULT_LOG_LEVEL_KEY, "DEBUG");

       try {
           SshClient ssh = SshClient.setUpDefaultClient();
           ssh.start();

           ssh.setServerKeyVerifier(AcceptAllServerKeyVerifier.INSTANCE); // 测试环境使用，生产环境需替换

           ConnectFuture future= ssh.connect(user,host, 22);

           this.session= future.verify(3000L).getSession();
           this.session.addPasswordIdentity(pass);
           this.session.auth().verify();

           channel = session.createShellChannel();
           channel.setPtyType("xterm");

           channel.open();

           out = channel.getInvertedIn();

           ShellTestTtyConnector connector = widget.createTtyConnector(Charset.defaultCharset());
           connector.init(channel);

           ZModemTtyConnector adaptor = new ZModemTtyConnector(widget.getTerminal(), connector);

           this.widget.openSession(adaptor);
       } catch (Exception e) {
           e.printStackTrace();
       }
   }

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

   public void disconnect() throws IOException {
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
       hostField.setText("120.24.176.61");
       Scene scene = new Scene(root);
       primaryStage.setScene(scene);
       primaryStage.setTitle("SSH Terminal");
       primaryStage.show();

   }

   public static class SSHTerminalAp4Test {

       public static void main(String[] args) throws URISyntaxException {
           ShellTerminalApp4.main(args);
       }

   }


}

