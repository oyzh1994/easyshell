package cn.oyzh.jeditermfx.terminal.ui;

import cn.oyzh.jeditermfx.terminal.Terminal;
import cn.oyzh.jeditermfx.terminal.TtyConnector;
import cn.oyzh.jeditermfx.terminal.model.TerminalTextBuffer;

/**
 * @author traff
 */
public interface TerminalSession {

    void start();

    TerminalTextBuffer getTerminalTextBuffer();

    Terminal getTerminal();

    TtyConnector getTtyConnector();

    void close();
}
