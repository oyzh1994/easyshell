package cn.oyzh.jeditermfx.terminal.model;


import com.jediterm.core.util.CellPosition;

public class TerminalResizeResult {

    private final CellPosition newCursor;

    TerminalResizeResult(CellPosition newCursor) {
        this.newCursor = newCursor;
    }

    public CellPosition getNewCursor() {
        return newCursor;
    }
}
