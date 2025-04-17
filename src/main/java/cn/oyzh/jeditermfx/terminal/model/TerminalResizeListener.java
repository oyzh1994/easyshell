package cn.oyzh.jeditermfx.terminal.model;

import cn.oyzh.jeditermfx.core.util.TermSize;

public interface TerminalResizeListener {

    void onResize(TermSize oldTermSize, TermSize newTermSize);
}
