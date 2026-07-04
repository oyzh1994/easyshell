package org.mosh4j.terminal;

import java.util.Arrays;

/**
 * Stateful ANSI renderer for {@link Framebuffer}.
 * <p>
 * It keeps a shadow copy of the last rendered frame and emits only changed lines
 * after the first full render. This provides a practical terminal frontend output
 * stream that can be consumed by terminal widgets or PTY-like bridges.
 */
public final class StatefulAnsiRenderer {

    private static final String ESC = "\u001B[";
    private static final String CSI_CLEAR_SCREEN = ESC + "2J";
    private static final String CSI_HOME = ESC + "H";
    private static final String CSI_ERASE_TO_END_OF_LINE = ESC + "K";
    private static final String CSI_SHOW_CURSOR = ESC + "?25h";
    private static final String CSI_HIDE_CURSOR = ESC + "?25l";

    private int lastWidth = -1;
    private int lastHeight = -1;
    private String[] lastRows = new String[0];
    private boolean initialized;

    /**
     * Render framebuffer to ANSI output. The first call emits a full screen redraw.
     * Later calls emit only changed rows (plus cursor state updates).
     */
    public synchronized String render(Framebuffer framebuffer) {
        if (framebuffer == null) {
            return "";
        }
        int width = Math.max(1, framebuffer.getWidth());
        int height = Math.max(1, framebuffer.getHeight());
        String[] rows = snapshotRows(framebuffer, width, height);

        StringBuilder out = new StringBuilder(Math.max(256, width * height / 2));
        boolean fullRedraw = !initialized || width != lastWidth || height != lastHeight;

        if (fullRedraw) {
            out.append(CSI_CLEAR_SCREEN).append(CSI_HOME);
            for (int r = 0; r < height; r++) {
                out.append(ESC).append(r + 1).append(";1H");
                out.append(rows[r]);
                out.append(CSI_ERASE_TO_END_OF_LINE);
            }
        } else {
            for (int r = 0; r < height; r++) {
                if (!rows[r].equals(lastRows[r])) {
                    out.append(ESC).append(r + 1).append(";1H");
                    out.append(rows[r]);
                    out.append(CSI_ERASE_TO_END_OF_LINE);
                }
            }
        }

        int cursorRow = Math.max(0, framebuffer.getCursorRow()) + 1;
        int cursorCol = Math.max(0, framebuffer.getCursorCol()) + 1;
        out.append(ESC).append(cursorRow).append(";").append(cursorCol).append("H");
        out.append(framebuffer.isCursorVisible() ? CSI_SHOW_CURSOR : CSI_HIDE_CURSOR);

        lastWidth = width;
        lastHeight = height;
        lastRows = rows;
        initialized = true;
        return out.toString();
    }

    /**
     * Reset renderer state. The next render() emits a full redraw.
     */
    public synchronized void reset() {
        lastWidth = -1;
        lastHeight = -1;
        lastRows = new String[0];
        initialized = false;
    }

    private static String[] snapshotRows(Framebuffer framebuffer, int width, int height) {
        String[] rows = new String[height];
        for (int row = 0; row < height; row++) {
            StringBuilder line = new StringBuilder(width);
            int usedColumns = 0;
            for (int col = 0; col < width; col++) {
                Cell cell = framebuffer.getCell(row, col);
                int[] cps = cell != null ? cell.getCodePoints() : null;
                if (cps == null || cps.length == 0) {
                    line.append(' ');
                    usedColumns++;
                    continue;
                }
                int appended = appendCell(line, cps);
                if (appended == 0) {
                    line.append(' ');
                    appended = 1;
                }
                usedColumns += appended;
                if (usedColumns > width) {
                    break;
                }
            }
            if (line.length() > width) {
                rows[row] = line.substring(0, width);
            } else {
                rows[row] = line.toString();
            }
        }
        return rows;
    }

    private static int appendCell(StringBuilder line, int[] cps) {
        int before = line.length();
        for (int cp : cps) {
            if (cp <= 0) {
                continue;
            }
            line.appendCodePoint(cp);
        }
        return line.length() - before;
    }

    @Override
    public synchronized String toString() {
        return "StatefulAnsiRenderer{" +
                "lastWidth=" + lastWidth +
                ", lastHeight=" + lastHeight +
                ", initialized=" + initialized +
                ", lastRowsHash=" + Arrays.hashCode(lastRows) +
                '}';
    }
}
