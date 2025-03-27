package cn.oyzh.easyshell.fx.svg.glyph.file;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class FileTsSVGGlyph extends SVGGlyph {

    public FileTsSVGGlyph() {
        super("/font/file-ts.svg");
    }

    public FileTsSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
