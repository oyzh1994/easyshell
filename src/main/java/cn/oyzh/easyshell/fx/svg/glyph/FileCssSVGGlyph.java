package cn.oyzh.easyshell.fx.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class FileCssSVGGlyph extends SVGGlyph {

    public FileCssSVGGlyph() {
        super("/font/file-css.svg");
    }

    public FileCssSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
