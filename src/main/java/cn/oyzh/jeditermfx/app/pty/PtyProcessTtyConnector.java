package cn.oyzh.jeditermfx.app.pty;

import com.pty4j.PtyProcess;
import com.pty4j.WinSize;
import cn.oyzh.jeditermfx.terminal.ProcessTtyConnector;
import cn.oyzh.jeditermfx.core.util.TermSize;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
        return "Local";
    }
}
