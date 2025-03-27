package cn.oyzh.easyshell.fx.svg.glyph.file;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class FileSVGGlyph extends SVGGlyph {

    public FileSVGGlyph() {
        super("/font/file.svg");
    }

    public FileSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
