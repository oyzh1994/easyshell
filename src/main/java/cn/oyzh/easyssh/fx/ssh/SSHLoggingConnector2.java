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
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

public final class SSHLoggingConnector2 extends PtyProcessTtyConnector implements LoggingTtyConnector {

    @Setter
    private int MAX_LOG_SIZE = 200;

    @NotNull
    private final LinkedList<char[]> myDataChunks = new LinkedList<>();

    @NotNull
    private final LinkedList<TerminalState> myStates = new LinkedList<>();

    @Nullable
    private TerminalTextBuffer textBuffer;

    private int logStart;

    private InputStreamReader sshReader;

//    private InputStream sshInput;

    private OutputStream sshOutput;

    public void setSshInput(InputStream sshInput) {
//        this.sshInput = sshInput;
        this.sshReader = new InputStreamReader(sshInput, Charsets.UTF_8);
    }

    public void setSshOutput(OutputStream sshOutput) {
        this.sshOutput = sshOutput;
    }

    public SSHLoggingConnector2(@NotNull PtyProcess process, @NotNull Charset charset, @NotNull List<String> commandLines) {
        super(process, charset, commandLines);
    }

    @Override
    public int read(char @NotNull [] buf, int offset, int length) throws IOException {
        if (sshReader == null) {
            return super.read(buf, offset, length);
        }
//        char[] buffer = new char[length];
//        int len1 = super.read(buffer, offset, length);
//        System.out.println(buffer);
        int len = sshReader.read(buf, offset, length);
//        int len = super.read(buf, offset, length);
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
//        super.write(string);
        this.sshOutput.write(string.getBytes(Charsets.UTF_8));
        this.sshOutput.flush();
    }

    @Override
    public void write(byte @NotNull [] bytes) throws IOException {
        JulLog.info("Writing in OutputStream : {}", Arrays.toString(bytes) + " " + new String(bytes, Charsets.UTF_8));
//        super.write(bytes);
        this.sshOutput.write(bytes);
        this.sshOutput.flush();
    }

    public void setWidget(@NotNull JediTermFxWidget widget) {
        this.textBuffer = widget.getTerminalTextBuffer();
    }
}