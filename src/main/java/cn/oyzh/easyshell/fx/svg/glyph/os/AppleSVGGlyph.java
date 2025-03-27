package cn.oyzh.easyshell.fx.svg.glyph.os;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class AppleSVGGlyph extends SVGGlyph {

    public AppleSVGGlyph() {
        super("/font/os/apple.svg");
    }

    public AppleSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
