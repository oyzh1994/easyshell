package cn.oyzh.jeditermfx.app.pty;

import com.jediterm.core.util.TermSize;
import com.jediterm.terminal.ProcessTtyConnector;
import com.pty4j.PtyProcess;
import com.pty4j.WinSize;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.List;

/**
 * @author traff
 */
public class PtyProcessTtyConnector extends ProcessTtyConnector {

    private final PtyProcess myProcess;

    public PtyProcessTtyConnector(@NotNull PtyProcess process, @NotNull Charset charset) {
        this(process, charset, null);
    }

    public PtyProcessTtyConnector(@NotNull PtyProcess process, @NotNull Charset charset, @Nullable List<String> commandLine) {
        super(process, charset, commandLine);
        myProcess = process;
    }

    @Override
    public void resize(@NotNull TermSize termSize) {
        if (isConnected()) {
            myProcess.setWinSize(new WinSize(termSize.getColumns(), termSize.getRows()));
        }
    }

    @Override
    public boolean isConnected() {
        return myProcess.isAlive();
    }

    @Override
    public String getName() {
        return "";
    }

    public InputStream input() throws IOException {
        return null;
    }

    public OutputStream output() throws IOException{
        return null;
    }
}
