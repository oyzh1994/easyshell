package cn.oyzh.easyshell.fx.svg.glyph.file.c;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class FileCSVGGlyph extends SVGGlyph {

    public FileCSVGGlyph() {
        super("/font/file/c/file-c.svg");
    }

    public FileCSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
