package cn.oyzh.easyshell.fx.svg.glyph.file.d;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class FileDotSVGGlyph extends SVGGlyph {

    public FileDotSVGGlyph() {
        super("/font/file/d/file-dot.svg");
    }

    public FileDotSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
