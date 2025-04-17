package com.techsenger.jeditermfx.terminal.model;

import com.techsenger.jeditermfx.core.util.TermSize;

public interface TerminalResizeListener {

    void onResize(TermSize oldTermSize, TermSize newTermSize);
}
