package cn.oyzh.jeditermfx.app.example;

import com.jediterm.terminal.ui.settings.FXDefaultSettingsProvider;
import com.jediterm.terminal.ui.FXJediTermWidget;
import com.jediterm.terminal.TtyConnector;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.PipedReader;
import java.io.PipedWriter;

public class BasicTerminalExample extends Application {

    private static final char ESC = 27;

    private static void writeTerminalCommands(@NotNull PipedWriter writer) throws IOException {
        writer.write(ESC + "%G");
        writer.write(ESC + "[31m");
        writer.write("Hello\r\n");
        writer.write(ESC + "[32;43m");
        writer.write("World\r\n");
    }

    private static @NotNull FXJediTermWidget createTerminalWidget() {
        FXJediTermWidget widget = new FXJediTermWidget(80, 24, new FXDefaultSettingsProvider());
        try (var terminalWriter = new PipedWriter()) {
            widget.setTtyConnector(new ExampleTtyConnector(terminalWriter));
            widget.start();
            writeTerminalCommands(terminalWriter);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return widget;
    }

    private static class ExampleTtyConnector implements TtyConnector {

        private final PipedReader myReader;

        public ExampleTtyConnector(@NotNull PipedWriter writer) {
            try {
                myReader = new PipedReader(writer);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void close() {
        }

        @Override
        public String getName() {
            return null;
        }

        @Override
        public int read(char[] buf, int offset, int length) throws IOException {
            return myReader.read(buf, offset, length);
        }

        @Override
        public void write(byte[] bytes) {
        }

        @Override
        public boolean isConnected() {
            return true;
        }

        @Override
        public void write(String string) {
        }

        @Override
        public int waitFor() {
            return 0;
        }

        @Override
        public boolean ready() throws IOException {
            return myReader.ready();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        var widget = createTerminalWidget();
        stage.setTitle("Basic Terminal Example");
        stage.setOnCloseRequest(event -> {
            widget.close();
            widget.getTtyConnector().close();
        });
        Scene scene = new Scene(widget.getComponent(), 600, 400);
        stage.setScene(scene);
        stage.show();
    }
}
