package cn.oyzh.easyshell.fx.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class FileUnknownSVGGlyph extends SVGGlyph {

    public FileUnknownSVGGlyph() {
        super("/font/file-unknown.svg");
    }

    public FileUnknownSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
