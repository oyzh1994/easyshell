package com.techsenger.jeditermfx.terminal.ui;

import com.techsenger.jeditermfx.terminal.Terminal;
import com.techsenger.jeditermfx.terminal.TtyConnector;
import com.techsenger.jeditermfx.terminal.model.TerminalTextBuffer;

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
