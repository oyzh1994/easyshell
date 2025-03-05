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

public final class SSHLoggingConnector3 extends PtyProcessTtyConnector implements LoggingTtyConnector {

    @Setter
    private int MAX_LOG_SIZE = 200;

    @NotNull
    private final LinkedList<char[]> myDataChunks = new LinkedList<>();

    @NotNull
    private final LinkedList<TerminalState> myStates = new LinkedList<>();

    @Nullable
    private TerminalTextBuffer textBuffer;

    private int logStart;

    public SSHLoggingConnector3(@NotNull PtyProcess process, @NotNull Charset charset, @NotNull List<String> commandLines) {
        super(process, charset, commandLines);
    }

    @Override
    public int read(char @NotNull [] buf, int offset, int length) throws IOException {
        int len = super.read(buf, offset, length);
        if (len > 0) {
            char[] arr = ArraysKt.copyOfRange(buf, offset, len);
            System.out.println(new String(arr) + "-------");
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
    public void write(@NotNull String string) throws IOException {
        JulLog.info("Writing in OutputStream : {}", string);
        super.write(string);
    }

    @Override
    public void write(byte @NotNull [] bytes) throws IOException {
        JulLog.info("Writing in OutputStream : {}", Arrays.toString(bytes) + " " + new String(bytes, Charsets.UTF_8));
        super.write(bytes);
    }

    public void setWidget(@NotNull JediTermFxWidget widget) {
        this.textBuffer = widget.getTerminalTextBuffer();
    }
}