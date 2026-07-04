package org.mosh4j.terminal;

/**
 * A single cell in the terminal framebuffer: character data and display attributes.
 */
public final class Cell {

    private final int[] codePoints;
    private final int width;
    private final int foreground;
    private final int background;
    private final int renditions;

    public Cell(int[] codePoints, int width, int foreground, int background, int renditions) {
        this.codePoints = codePoints == null ? new int[0] : codePoints.clone();
        this.width = width;
        this.foreground = foreground;
        this.background = background;
        this.renditions = renditions;
    }

    public static Cell blank() {
        return new Cell(new int[] { ' ' }, 1, 0, 0, 0);
    }

    public int[] getCodePoints() {
        return codePoints.clone();
    }

    public int getWidth() {
        return width;
    }

    public int getForeground() {
        return foreground;
    }

    public int getBackground() {
        return background;
    }

    public int getRenditions() {
        return renditions;
    }

    public boolean isBlank() {
        return codePoints.length == 1 && codePoints[0] == ' ' && width == 1;
    }
}
