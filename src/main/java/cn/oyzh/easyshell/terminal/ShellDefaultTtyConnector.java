package cn.oyzh.easyshell.terminal;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.thread.ThreadUtil;
import cn.oyzh.jeditermfx.app.pty.PtyProcessTtyConnector;
import com.jediterm.core.util.TermSize;
import com.pty4j.PtyProcess;
import com.pty4j.WinSize;
import javafx.beans.property.SimpleObjectProperty;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

/**
 * @author oyzh
 * @since 2025-03-04
 */
public class ShellDefaultTtyConnector extends PtyProcessTtyConnector {
// public class ShellDefaultTtyConnector extends PtyProcessTtyConnector implements LoggingTtyConnector {

//    protected int maxLogSize = 200;
//
//    public void setMaxLogSize(int maxLogSize) {
//        this.maxLogSize = maxLogSize;
//    }
//
//    public int getMaxLogSize() {
//        return maxLogSize;
//    }
//
//    protected final LinkedList<char[]> myDataChunks = new LinkedList<>();
//
//    protected final LinkedList<TerminalState> myStates = new LinkedList<>();
//
//    protected TerminalTextBuffer textBuffer;
//
//    protected int logStart;

    public ShellDefaultTtyConnector(PtyProcess process, Charset charset, List<String> commandLines) {
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

    protected int doRead(char[] buf, int offset, int len) throws IOException {
        if(JulLog.isDebugEnabled()) {
            JulLog.debug("shell read: {}", new String(buf));
        }
        return len;
    }

//    @Override
//    public List<char[]> getChunks() {
//        return new ArrayList<>(this.myDataChunks);
//    }
//
//    @Override
//    public List<TerminalState> getStates() {
//        return new ArrayList<>(this.myStates);
//    }
//
//    @Override
//    public int getLogStart() {
//        return this.logStart;
//    }

    public void writeLine(String str) throws IOException {
        this.write(str + "\r");
    }

    @Override
    public void write(String str) throws IOException {
        if(JulLog.isDebugEnabled()) {
            JulLog.debug("shell write : {}", str);
        }
        super.write(str);
    }

    @Override
    public void write(byte[] bytes) throws IOException {
        String str = new String(bytes, this.myCharset);
        if(JulLog.isDebugEnabled()) {
            JulLog.debug("shell write : {}", str);
        }
        super.write(bytes);
    }

//    public void setWidget(FXJediTermWidget widget) {
    /// /        this.textBuffer = widget.getTerminalTextBuffer();
//    }

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

    /**
     * 重置tty连接器回调
     */
    private Runnable resetTtyConnectorCallback;

    public Runnable getResetTtyConnectorCallback() {
        return resetTtyConnectorCallback;
    }

    public void setResetTtyConnectorCallback(Runnable resetTtyConnectorCallback) {
        this.resetTtyConnectorCallback = resetTtyConnectorCallback;
    }

    /**
     * 重置tty连接器
     */
    public void resetTtyConnector() {
        if (this.resetTtyConnectorCallback != null) {
            // 只允许执行一次，执行完成就销毁
            Runnable func = this.resetTtyConnectorCallback;
            this.resetTtyConnectorCallback = null;
            func.run();
        }
    }
}