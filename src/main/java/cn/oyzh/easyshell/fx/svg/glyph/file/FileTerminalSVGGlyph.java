package cn.oyzh.easyshell.fx.svg.glyph.file;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class FileTerminalSVGGlyph extends SVGGlyph {

    public FileTerminalSVGGlyph() {
        super("/font/file-terminal.svg");
    }

    public FileTerminalSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
