package cn.oyzh.easyshell.ssh;

import cn.oyzh.common.log.JulLog;
import com.pty4j.PtyProcess;
import com.pty4j.WinSize;
import com.techsenger.jeditermfx.app.debug.TerminalDebugUtil;
import com.techsenger.jeditermfx.app.pty.LoggingTtyConnector;
import com.techsenger.jeditermfx.app.pty.PtyProcessTtyConnector;
import com.techsenger.jeditermfx.core.model.TerminalTextBuffer;
import com.techsenger.jeditermfx.core.util.TermSize;
import com.techsenger.jeditermfx.ui.JediTermFxWidget;
import javafx.beans.property.SimpleObjectProperty;
import kotlin.collections.ArraysKt;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * @author oyzh
 * @since 2025-03-04
 */
public class ShellTtyConnector extends PtyProcessTtyConnector implements LoggingTtyConnector {

    @Setter
    private int MAX_LOG_SIZE = 200;

    @NotNull
    private final LinkedList<char[]> myDataChunks = new LinkedList<>();

    @NotNull
    private final LinkedList<TerminalState> myStates = new LinkedList<>();

    @Nullable
    private TerminalTextBuffer textBuffer;

    private int logStart;

    private InputStreamReader shellReader;

    private OutputStreamWriter shellWriter;

    public void initShell(SSHShell shell) throws IOException {
        this.shellReader = new InputStreamReader(shell.getInputStream(), this.myCharset);
        this.shellWriter = new OutputStreamWriter(shell.getOutputStream(), this.myCharset);
    }

    public ShellTtyConnector(@NotNull PtyProcess process, @NotNull Charset charset, @NotNull List<String> commandLines) {
        super(process, charset, commandLines);
    }

    @Override
    public int read(char @NotNull [] buf, int offset, int length) throws IOException {
        int len;
        if (this.shellReader == null) {
            len = super.read(buf, offset, length);
        } else {
            len = this.shellReader.read(buf, offset, length);
        }
        if (len > 0) {
            char[] arr = ArraysKt.copyOfRange(buf, offset, len);
            JulLog.debug("shell read: {}", new String(arr));
            this.myDataChunks.add(arr);
            String lines = this.textBuffer.getScreenLines();
            TerminalState terminalState =
                    new TerminalState(lines, TerminalDebugUtil.getStyleLines(textBuffer),
                            this.textBuffer.getHistoryBuffer().getLines());
            this.myStates.add(terminalState);
            if (this.myDataChunks.size() > this.MAX_LOG_SIZE) {
                this.myDataChunks.removeFirst();
                this.myStates.removeFirst();
                this.logStart++;
            }
        }
        return len;
    }

    @NotNull
    @Override
    public List<char[]> getChunks() {
        return new ArrayList<>(this.myDataChunks);
    }

    @NotNull
    @Override
    public List<TerminalState> getStates() {
        return new ArrayList<>(this.myStates);
    }

    @Override
    public int getLogStart() {
        return this.logStart;
    }

    @Override
    public void write(@NotNull String str) throws IOException {
        JulLog.debug("shell write : {}", str);
//        super.write(string);
        this.shellWriter.write(str);
        this.shellWriter.flush();
    }

    @Override
    public void write(byte @NotNull [] bytes) throws IOException {
        String str = new String(bytes, this.myCharset);
        JulLog.debug("shell write : {}", str);
//        super.write(bytes);
        this.shellWriter.write(str);
        this.shellWriter.flush();
    }

    public void setWidget(@NotNull JediTermFxWidget widget) {
        this.textBuffer = widget.getTerminalTextBuffer();
    }

    private SimpleObjectProperty<TermSize> terminalSizeProperty;

    public SimpleObjectProperty<TermSize> terminalSizeProperty() {
        if (this.terminalSizeProperty == null) {
            this.terminalSizeProperty = new SimpleObjectProperty<>(this.getTermSize());
        }
        return this.terminalSizeProperty;
    }

    @Override
    public void resize(@NotNull TermSize termSize) {
        super.resize(termSize);
        if (this.terminalSizeProperty != null) {
            this.terminalSizeProperty.set(termSize);
        }
    }

    public TermSize getTermSize() {
        WinSize winSize = this.getWinSize();
        if (winSize != null) {
            return new TermSize(winSize.getColumns(), winSize.getRows());
        }
        return null;
    }

    public WinSize getWinSize() {
        try {
            return this.getProcess().getWinSize();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public @NotNull PtyProcess getProcess() {
        return (PtyProcess) super.getProcess();
    }
}