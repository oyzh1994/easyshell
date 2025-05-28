package com.jediterm.terminal.ui;

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
