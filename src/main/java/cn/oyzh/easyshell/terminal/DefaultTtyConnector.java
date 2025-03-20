package cn.oyzh.easyshell.terminal;

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

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * @author oyzh
 * @since 2025-03-04
 */
public class DefaultTtyConnector extends PtyProcessTtyConnector implements LoggingTtyConnector {

    protected int maxLogSize = 200;

    public void setMaxLogSize(int maxLogSize) {
        this.maxLogSize = maxLogSize;
    }

    public int getMaxLogSize() {
        return maxLogSize;
    }

    protected final LinkedList<char[]> myDataChunks = new LinkedList<>();

    protected final LinkedList<TerminalState> myStates = new LinkedList<>();

    protected TerminalTextBuffer textBuffer;

    protected int logStart;

    public DefaultTtyConnector(PtyProcess process, Charset charset, List<String> commandLines) {
        super(process, charset, commandLines);
    }

    @Override
    public int read(char[] buf, int offset, int length) throws IOException {
        int len = super.read(buf, offset, length);
        if (len > 0) {
            this.doRead(buf, offset, len);
        }
        return len;
    }

    protected void doRead(char[] buf, int offset, int len) throws IOException {
//        char[] arr = ArraysKt.copyOfRange(buf, offset, len);
        char[] arr;
        if (len != buf.length) {
            arr = ArraysKt.copyOfRange(buf, offset, len);
        } else {
            arr = buf;
        }
        JulLog.debug("shell read: {}", new String(arr));
        this.myDataChunks.add(arr);
        String lines = this.textBuffer.getScreenLines();
        TerminalState terminalState =
                new TerminalState(lines, TerminalDebugUtil.getStyleLines(this.textBuffer),
                        this.textBuffer.getHistoryBuffer().getLines());
        this.myStates.add(terminalState);
        if (this.myDataChunks.size() > this.maxLogSize) {
            this.myDataChunks.removeFirst();
            this.myStates.removeFirst();
            this.logStart++;
        }

    }

    @Override
    public List<char[]> getChunks() {
        return new ArrayList<>(this.myDataChunks);
    }

    @Override
    public List<TerminalState> getStates() {
        return new ArrayList<>(this.myStates);
    }

    @Override
    public int getLogStart() {
        return this.logStart;
    }

    @Override
    public void write(String str) throws IOException {
        JulLog.debug("shell write : {}", str);
        super.write(str);
    }

    @Override
    public void write(byte[] bytes) throws IOException {
        String str = new String(bytes, this.myCharset);
        JulLog.debug("shell write : {}", str);
        super.write(bytes);
    }

    public void setWidget(JediTermFxWidget widget) {
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
    public void resize(TermSize termSize) {
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
    public PtyProcess getProcess() {
        return (PtyProcess) super.getProcess();
    }
}