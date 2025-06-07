package cn.oyzh.easyshell.fx.svg.glyph.file;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class FileTsxSVGGlyph extends SVGGlyph {

    public FileTsxSVGGlyph() {
        super("/font/file/file-tsx.svg");
    }

    public FileTsxSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
