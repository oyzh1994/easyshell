package cn.oyzh.easyshell.fx.svg.glyph.file.w;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class FileWordSVGGlyph extends SVGGlyph {

    public FileWordSVGGlyph() {
        super("/font/file/w/file-word.svg");
    }

    public FileWordSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
