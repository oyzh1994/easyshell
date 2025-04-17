package com.techsenger.jeditermfx.terminal;

import com.techsenger.jeditermfx.core.Color;
import com.techsenger.jeditermfx.terminal.emulator.mouse.MouseFormat;
import com.techsenger.jeditermfx.terminal.emulator.mouse.MouseMode;
import com.techsenger.jeditermfx.terminal.model.TerminalSelection;
import com.techsenger.jeditermfx.core.util.TermSize;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface TerminalDisplay {

    void setCursor(int x, int y);

    /**
     * Sets cursor shape, null means default.
     */
    void setCursorShape(@Nullable CursorShape cursorShape);

    void beep();

    default void onResize(@NotNull TermSize newTermSize, @NotNull RequestOrigin origin) {
    }

    void scrollArea(final int scrollRegionTop, final int scrollRegionSize, int dy);

    void setCursorVisible(boolean isCursorVisible);

    void useAlternateScreenBuffer(boolean useAlternateScreenBuffer);

    String getWindowTitle();

    void setWindowTitle(@NotNull String windowTitle);

    @Nullable
    TerminalSelection getSelection();

    void terminalMouseModeSet(@NotNull MouseMode mouseMode);

    void setMouseFormat(@NotNull MouseFormat mouseFormat);

    boolean ambiguousCharsAreDoubleWidth();

    default void setBracketedPasteMode(boolean bracketedPasteModeEnabled) {
    }

    default @Nullable Color getWindowForeground() {
        return null;
    }

    default @Nullable Color getWindowBackground() {
        return null;
    }
}
