package cn.oyzh.easyshell.test;

import com.jcraft.jsch.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import org.fusesource.jansi.Ansi;
import org.fusesource.jansi.AnsiConsole;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;

public class ShellTerminalApp extends Application {

    private TextFlow outputArea;

    @Override
    public void start(Stage primaryStage) {
        outputArea = new TextFlow();

        VBox vbox = new VBox(outputArea);
        Scene scene = new Scene(vbox, 800, 600);

        primaryStage.setScene(scene);
        primaryStage.setTitle("SSH Terminal");
        primaryStage.show();

        AnsiConsole.systemInstall();
        connectAndExecute();
    }

    private void connectAndExecute() {
        new Thread(() -> {
            try {
                JSch jsch = new JSch();
                Session session = jsch.getSession("", "", 22);
                session.setPassword("");
                session.setConfig("StrictHostKeyChecking", "no");
                session.connect();

                Channel channel = session.openChannel("exec");
                ((ChannelExec) channel).setCommand("ls\n");
                channel.setInputStream(null);
                ((ChannelExec) channel).setErrStream(System.err);

                channel.connect();
                InputStream in = channel.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));

                String line;
                while ((line = reader.readLine()) != null) {
                    Ansi ansi = Ansi.ansi().reset().a(line);
                    String[] parts = ansi.toString().split("\u001B\\[");
                    Platform.runLater(() -> {
                        for (String part : parts) {
                            if (part.isEmpty()) continue;
                            int mIndex = part.indexOf('m');
                            if (mIndex != -1) {
                                String code = part.substring(0, mIndex);
                                String text = part.substring(mIndex + 1);
                                Color color = getColorFromAnsiCode(code);
                                Text textNode = new Text(text);
                                textNode.setFill(color);
                                outputArea.getChildren().add(textNode);
                            } else {
                                Text textNode = new Text(part);
                                outputArea.getChildren().add(textNode);
                            }
                        }
                        outputArea.getChildren().add(new Text("\n"));
                    });
                }

                channel.disconnect();
                session.disconnect();
            } catch (JSchException | java.io.IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private Color getColorFromAnsiCode(String code) {
        switch (code) {
            case "31": return Color.RED;
            case "32": return Color.GREEN;
            case "33": return Color.YELLOW;
            case "34": return Color.BLUE;
            case "35": return Color.MAGENTA;
            case "36": return Color.CYAN;
            case "37": return Color.WHITE;
            default: return Color.BLACK;
        }
    }

    public static void main(String[] args) {
        launch(args);
    }


    public static class SSHTerminalAppTest {

        public static void main(String[] args) throws URISyntaxException {
            ShellTerminalApp.main(args);
        }

    }


}

