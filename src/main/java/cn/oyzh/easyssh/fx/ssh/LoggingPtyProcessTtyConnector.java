package cn.oyzh.easyssh.fx.ssh;

import cn.oyzh.common.log.JulLog;
import com.pty4j.PtyProcess;
import com.techsenger.jeditermfx.app.debug.TerminalDebugUtil;
import com.techsenger.jeditermfx.app.pty.LoggingTtyConnector;
import com.techsenger.jeditermfx.app.pty.PtyProcessTtyConnector;
import com.techsenger.jeditermfx.core.model.TerminalTextBuffer;
import com.techsenger.jeditermfx.ui.JediTermFxWidget;
import kotlin.collections.ArraysKt;
import kotlin.jvm.internal.Intrinsics;
import kotlin.text.Charsets;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public final class LoggingPtyProcessTtyConnector extends PtyProcessTtyConnector implements LoggingTtyConnector {

    private final int MAX_LOG_SIZE = 200;

    @NotNull
    private final LinkedList<char[]> myDataChunks = new LinkedList<>();

    @NotNull
    private final LinkedList<TerminalState> myStates = new LinkedList<>();

    @Nullable
    private JediTermFxWidget myWidget;

    private int logStart;

    public LoggingPtyProcessTtyConnector(@NotNull PtyProcess process, @NotNull Charset charset, @NotNull List command) {
        super(process, charset, command);
        Intrinsics.checkNotNullParameter(process, "process");
        Intrinsics.checkNotNullParameter(charset, "charset");
        Intrinsics.checkNotNullParameter(command, "command");
    }

    @Override
    public int read(@NotNull char[] buf, int offset, int length) throws IOException {
        Intrinsics.checkNotNullParameter(buf, "buf");
        int len = super.read(buf, offset, length);
        if (len > 0) {
            char[] arr = ArraysKt.copyOfRange(buf, offset, len);
            this.myDataChunks.add(arr);
            Intrinsics.checkNotNull(this.myWidget);
            TerminalTextBuffer terminalTextBuffer = this.myWidget.getTerminalTextBuffer();
            String lines = terminalTextBuffer.getScreenLines();
            Intrinsics.checkNotNull(terminalTextBuffer);
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
        return new ArrayList(this.myDataChunks);
    }

    @NotNull
    @Override
    public List<TerminalState> getStates() {
        return new ArrayList(this.myStates);
    }

    @Override
    public int getLogStart() {
        return this.logStart;
    }

    @Override
    public void write(@NotNull String string) throws IOException {
        Intrinsics.checkNotNullParameter(string, "string");
        JulLog.debug("Writing in OutputStream : " + string);
        super.write(string);
    }

    @Override
    public void write(@NotNull byte[] bytes) throws IOException {
        Intrinsics.checkNotNullParameter(bytes, "bytes");
        JulLog.debug("Writing in OutputStream : " + Arrays.toString(bytes) + " " + new String(bytes, Charsets.UTF_8));
        super.write(bytes);
    }

    public final void setWidget(@NotNull JediTermFxWidget widget) {
        Intrinsics.checkNotNullParameter(widget, "widget");
        this.myWidget = widget;
    }
}