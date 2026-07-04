package org.mosh4j.terminal;

import java.nio.charset.StandardCharsets;

/**
 * Minimal framebuffer: fixed size grid, simple ANSI handling (cursor move, clear, basic SGR).
 */
public class SimpleFramebuffer implements Framebuffer {

    private int width;
    private int height;
    private Cell[][] cells;
    private int cursorRow;
    private int cursorCol;
    private boolean cursorVisible = true;
    private String title = "";
    private final MinimalAnsiParser ansiParser;

    public SimpleFramebuffer(int width, int height) {
        this.width = Math.max(1, width);
        this.height = Math.max(1, height);
        this.cells = new Cell[height][width];
        for (int r = 0; r < height; r++) {
            for (int c = 0; c < width; c++) {
                cells[r][c] = Cell.blank();
            }
        }
        this.cursorRow = 0;
        this.cursorCol = 0;
        this.ansiParser = new MinimalAnsiParser(this);
    }

    @Override
    public synchronized int getWidth() {
        return width;
    }

    @Override
    public synchronized int getHeight() {
        return height;
    }

    @Override
    public synchronized Cell getCell(int row, int col) {
        if (row >= 0 && row < height && col >= 0 && col < width) {
            return cells[row][col];
        }
        return Cell.blank();
    }

    synchronized void setCell(int row, int col, Cell cell) {
        if (row >= 0 && row < height && col >= 0 && col < width) {
            cells[row][col] = cell;
        }
    }

    @Override
    public int getCursorRow() {
        return cursorRow;
    }

    @Override
    public int getCursorCol() {
        return cursorCol;
    }

    @Override
    public boolean isCursorVisible() {
        return cursorVisible;
    }

    @Override
    public String getTitle() {
        return title;
    }

    void setCursor(int row, int col) {
        this.cursorRow = clamp(row, 0, height - 1);
        this.cursorCol = clamp(col, 0, width - 1);
    }

    void setCursorVisible(boolean visible) {
        this.cursorVisible = visible;
    }

    void setTitle(String title) {
        this.title = title != null ? title : "";
    }

    private static int clamp(int v, int lo, int hi) {
        return Math.max(lo, Math.min(hi, v));
    }

    public synchronized void resize(int newWidth, int newHeight) {
        newWidth = Math.max(1, newWidth);
        newHeight = Math.max(1, newHeight);
        if (newWidth == width && newHeight == height) return;
        Cell[][] newCells = new Cell[newHeight][newWidth];
        for (int r = 0; r < newHeight; r++) {
            for (int c = 0; c < newWidth; c++) {
                if (r < height && c < width) {
                    newCells[r][c] = cells[r][c];
                } else {
                    newCells[r][c] = Cell.blank();
                }
            }
        }
        this.cells = newCells;
        this.width = newWidth;
        this.height = newHeight;
        this.cursorRow = clamp(cursorRow, 0, newHeight - 1);
        this.cursorCol = clamp(cursorCol, 0, newWidth - 1);
    }

    @Override
    public synchronized void feedHostBytes(byte[] bytes) {
        if (bytes == null || bytes.length == 0) return;
        ansiParser.feed(bytes);
    }

    @Override
    public synchronized byte[] toStateBytes() {
        StringBuilder sb = new StringBuilder();
        sb.append("W").append(width).append("H").append(height).append("R").append(cursorRow).append("C").append(cursorCol).append("\n");
        for (int r = 0; r < height; r++) {
            for (int c = 0; c < width; c++) {
                Cell cell = cells[r][c];
                int[] cp = cell.getCodePoints();
                for (int ch : cp) sb.appendCodePoint(ch);
            }
            sb.append("\n");
        }
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public synchronized void fromStateBytes(byte[] bytes) {
        if (bytes == null || bytes.length == 0) return;
        String s = new String(bytes, StandardCharsets.UTF_8);
        int i = 0;
        if (s.startsWith("W") && s.contains("H")) {
            int wEnd = s.indexOf('H');
            int hEnd = s.indexOf('R');
            if (wEnd > 0 && hEnd > wEnd) {
                try {
                    int w = Integer.parseInt(s.substring(1, wEnd));
                    int h = Integer.parseInt(s.substring(wEnd + 1, hEnd));
                    if (w != width || h != height || w <= 0 || h <= 0 || w > 10000 || h > 10000) {
                        return;
                    }
                    int cEnd = s.indexOf('\n', hEnd);
                    int cIdx = s.indexOf('C', hEnd);
                    if (cEnd <= 0 || cIdx <= hEnd) {
                        return;
                    } else {
                        try {
                            cursorRow = Integer.parseInt(s.substring(hEnd + 1, cIdx));
                            cursorCol = Integer.parseInt(s.substring(cIdx + 1, cEnd));
                            cursorRow = Math.max(0, Math.min(cursorRow, height - 1));
                            cursorCol = Math.max(0, Math.min(cursorCol, width - 1));
                        } catch (NumberFormatException e) {
                            cursorRow = 0;
                            cursorCol = 0;
                        }
                        i = cEnd + 1;
                    }
                } catch (NumberFormatException | IndexOutOfBoundsException ignored) {
                    return;
                }
            }
        }
        int row = 0, col = 0;
        while (i < s.length() && row < height) {
            char ch = s.charAt(i);
            if (ch == '\n') {
                row++;
                col = 0;
                i++;
                continue;
            }
            if (col < width) {
                int codePoint = Character.codePointAt(s, i);
                setCell(row, col, new Cell(new int[] { codePoint }, 1, 0, 0, 0));
                col++;
                i += Character.charCount(codePoint);
            } else {
                i++;
            }
        }
    }
}
