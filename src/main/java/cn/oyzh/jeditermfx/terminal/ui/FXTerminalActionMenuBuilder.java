package cn.oyzh.jeditermfx.terminal.ui;

import com.jediterm.terminal.ui.TerminalAction;
import com.jediterm.terminal.ui.TerminalActionMenuBuilder;
import org.jetbrains.annotations.NotNull;

public interface FXTerminalActionMenuBuilder extends TerminalActionMenuBuilder {

    @Override
    default void addAction(@NotNull TerminalAction action) {
        if (action instanceof FXTerminalAction terminalAction) {
            this.addAction(terminalAction);
        }
    }

    void addAction(@NotNull FXTerminalAction action);

}
