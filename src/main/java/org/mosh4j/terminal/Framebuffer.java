package org.mosh4j.terminal;

/**
 * Terminal framebuffer: width x height grid of cells, cursor position, and optional title.
 */
public interface Framebuffer {

    int getWidth();

    int getHeight();

    Cell getCell(int row, int col);

    int getCursorRow();

    int getCursorCol();

    boolean isCursorVisible();

    String getTitle();

    /**
     * Apply host output bytes (e.g. ANSI sequences + plain text) to update the framebuffer.
     * This is the minimal "apply" for SSP: interpret bytes and mutate state.
     */
    void feedHostBytes(byte[] bytes);

    /**
     * Serialize current framebuffer state to bytes (for diff/state sync). Minimal implementation
     * may return a simple encoding (e.g. row-by-row cell data).
     */
    byte[] toStateBytes();

    /**
     * Replace state from bytes (inverse of toStateBytes). Used when applying a diff that references
     * a full state.
     */
    void fromStateBytes(byte[] bytes);
}
