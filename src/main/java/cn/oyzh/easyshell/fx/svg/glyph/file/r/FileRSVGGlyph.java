package cn.oyzh.easyshell.fx.svg.glyph.file.r;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class FileRSVGGlyph extends SVGGlyph {

    public FileRSVGGlyph() {
        super("/font/file/r/file-r.svg");
    }

    public FileRSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
