package cn.oyzh.easyssh.fx.ssh;

import cn.oyzh.common.log.JulLog;
import com.pty4j.PtyProcess;
import com.techsenger.jeditermfx.app.debug.TerminalDebugUtil;
import com.techsenger.jeditermfx.app.pty.LoggingTtyConnector;
import com.techsenger.jeditermfx.app.pty.PtyProcessTtyConnector;
import com.techsenger.jeditermfx.core.model.TerminalTextBuffer;
import com.techsenger.jeditermfx.ui.JediTermFxWidget;
import kotlin.collections.ArraysKt;
import kotlin.text.Charsets;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public final class SSHLoggingConnector extends PtyProcessTtyConnector implements LoggingTtyConnector {

    @Setter
    private int MAX_LOG_SIZE = 200;

    @NotNull
    private final LinkedList<char[]> myDataChunks = new LinkedList<>();

    @NotNull
    private final LinkedList<TerminalState> myStates = new LinkedList<>();

    @Nullable
    private JediTermFxWidget myWidget;

    private int logStart;

    public SSHLoggingConnector(@NotNull PtyProcess process, @NotNull Charset charset, @NotNull List<String> commandLines) {
        super(process, charset, commandLines);
    }

    @Override
    public int read(char @NotNull [] buf, int offset, int length) throws IOException {
        int len = super.read(buf, offset, length);
        if (len > 0) {
            char[] arr = ArraysKt.copyOfRange(buf, offset, len);
            this.myDataChunks.add(arr);
            TerminalTextBuffer terminalTextBuffer = this.myWidget.getTerminalTextBuffer();
            String lines = terminalTextBuffer.getScreenLines();
            TerminalState terminalState =
                    new TerminalState(lines, TerminalDebugUtil.getStyleLines(terminalTextBuffer),
                            terminalTextBuffer.getHistoryBuffer().getLines());
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
    public void write(@NotNull String string) throws IOException {
        JulLog.debug("Writing in OutputStream : " + string);
        super.write(string);
    }

    @Override
    public void write(byte @NotNull [] bytes) throws IOException {
        JulLog.debug("Writing in OutputStream : " + Arrays.toString(bytes) + " " + new String(bytes, Charsets.UTF_8));
        super.write(bytes);
    }

    public void setWidget(@NotNull JediTermFxWidget widget) {
        this.myWidget = widget;
    }
}