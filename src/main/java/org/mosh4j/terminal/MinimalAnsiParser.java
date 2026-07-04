package org.mosh4j.terminal;

import java.util.ArrayList;
import java.util.List;

/**
 * Minimal ANSI/ECMA-48 parser: CSI sequences for cursor movement, erase, and basic SGR (colors).
 */
public final class MinimalAnsiParser {

    private final SimpleFramebuffer buffer;
    private final StringBuilder plain = new StringBuilder();
    private final List<Integer> csiParams = new ArrayList<>();
    private int state = 0;
    private static final int MAX_CSI_PARAMS = 32;
    private static final int MAX_CSI_PARAM_VALUE = 65536;
    private static final int PLAIN = 0;
    private static final int ESC = 1;
    private static final int CSI = 2;
    private static final int CSI_PARAM = 3;

    public MinimalAnsiParser(SimpleFramebuffer buffer) {
        this.buffer = buffer;
    }

    public void feed(byte[] bytes) {
        if (bytes == null) return;
        for (byte b : bytes) {
            feedByte(b & 0xFF);
        }
    }

    private void feedByte(int b) {
        char c = (char) b;
        switch (state) {
            case PLAIN -> {
                if (b == 0x1B) {
                    state = ESC;
                } else if (b == '\r') {
                    buffer.setCursor(buffer.getCursorRow(), 0);
                } else if (b == '\n') {
                    int r = buffer.getCursorRow();
                    buffer.setCursor(Math.min(r + 1, buffer.getHeight() - 1), buffer.getCursorCol());
                } else if (b == '\t') {
                    int col = buffer.getCursorCol();
                    int next = (col + 8) & ~7;
                    buffer.setCursor(buffer.getCursorRow(), Math.min(next, buffer.getWidth() - 1));
                } else {
                    putChar(c);
                }
            }
            case ESC -> {
                if (c == '[') {
                    state = CSI;
                    csiParams.clear();
                } else {
                    state = PLAIN;
                }
            }
            case CSI, CSI_PARAM -> {
                if (c >= '0' && c <= '9') {
                    if (state == CSI) {
                        if (csiParams.size() >= MAX_CSI_PARAMS) {
                            state = PLAIN;
                            break;
                        }
                        csiParams.add(0);
                        state = CSI_PARAM;
                    }
                    int last = csiParams.size() - 1;
                    int prev = csiParams.get(last);
                    int next = prev * 10 + (c - '0');
                    if (next > MAX_CSI_PARAM_VALUE) {
                        state = PLAIN;
                        break;
                    }
                    csiParams.set(last, next);
                } else if (c == ';') {
                    if (csiParams.size() >= MAX_CSI_PARAMS) {
                        state = PLAIN;
                        break;
                    }
                    csiParams.add(0);
                } else if (c == 'm') {
                    handleSgr();
                    state = PLAIN;
                } else if (c == 'H' || c == 'f') {
                    handleCup();
                    state = PLAIN;
                } else if (c == 'J') {
                    handleEd();
                    state = PLAIN;
                } else if (c == 'K') {
                    handleEl();
                    state = PLAIN;
                } else if (c == 'A') {
                    handleCuu();
                    state = PLAIN;
                } else if (c == 'B') {
                    handleCud();
                    state = PLAIN;
                } else if (c == 'C') {
                    handleCuf();
                    state = PLAIN;
                } else if (c == 'D') {
                    handleCub();
                    state = PLAIN;
                } else {
                    state = PLAIN;
                }
            }
            default -> state = PLAIN;
        }
    }

    private void putChar(char c) {
        int row = buffer.getCursorRow();
        int col = buffer.getCursorCol();
        if (row < buffer.getHeight() && col < buffer.getWidth()) {
            buffer.setCell(row, col, new Cell(new int[] { c }, 1, 0, 0, 0));
            if (col + 1 < buffer.getWidth()) {
                buffer.setCursor(row, col + 1);
            } else {
                buffer.setCursor(row, col);
            }
        }
    }

    private int getParam(int index, int defaultVal) {
        if (index >= csiParams.size()) return defaultVal;
        int v = csiParams.get(index);
        return v == 0 ? defaultVal : v;
    }

    private void handleCup() {
        int row = getParam(0, 1) - 1;
        int col = getParam(1, 1) - 1;
        buffer.setCursor(Math.max(0, row), Math.max(0, col));
    }

    private void handleEd() {
        int mode = getParam(0, 0);
        if (mode == 0) {
            for (int r = buffer.getCursorRow(); r < buffer.getHeight(); r++) {
                for (int c = 0; c < buffer.getWidth(); c++) {
                    if (r == buffer.getCursorRow() && c < buffer.getCursorCol()) continue;
                    buffer.setCell(r, c, Cell.blank());
                }
            }
        } else if (mode == 1) {
            for (int r = 0; r <= buffer.getCursorRow(); r++) {
                for (int c = 0; c < buffer.getWidth(); c++) {
                    if (r == buffer.getCursorRow() && c > buffer.getCursorCol()) continue;
                    buffer.setCell(r, c, Cell.blank());
                }
            }
        } else if (mode == 2) {
            for (int r = 0; r < buffer.getHeight(); r++) {
                for (int c = 0; c < buffer.getWidth(); c++) {
                    buffer.setCell(r, c, Cell.blank());
                }
            }
        }
    }

    private void handleEl() {
        int mode = getParam(0, 0);
        int row = buffer.getCursorRow();
        if (mode == 0) {
            for (int c = buffer.getCursorCol(); c < buffer.getWidth(); c++) {
                buffer.setCell(row, c, Cell.blank());
            }
        } else if (mode == 1) {
            for (int c = 0; c <= buffer.getCursorCol(); c++) {
                buffer.setCell(row, c, Cell.blank());
            }
        } else if (mode == 2) {
            for (int c = 0; c < buffer.getWidth(); c++) {
                buffer.setCell(row, c, Cell.blank());
            }
        }
    }

    private void handleCuu() {
        int n = getParam(0, 1);
        buffer.setCursor(buffer.getCursorRow() - n, buffer.getCursorCol());
    }

    private void handleCud() {
        int n = getParam(0, 1);
        buffer.setCursor(buffer.getCursorRow() + n, buffer.getCursorCol());
    }

    private void handleCuf() {
        int n = getParam(0, 1);
        buffer.setCursor(buffer.getCursorRow(), buffer.getCursorCol() + n);
    }

    private void handleCub() {
        int n = getParam(0, 1);
        buffer.setCursor(buffer.getCursorRow(), buffer.getCursorCol() - n);
    }

    private void handleSgr() {
        for (int i = 0; i < csiParams.size(); i++) {
            int p = csiParams.get(i);
            if (p == 0) {
                // reset - no-op for minimal
            } else if (p >= 30 && p <= 37) {
                // foreground - no-op for minimal
            } else if (p >= 40 && p <= 47) {
                // background - no-op for minimal
            }
        }
    }
}
