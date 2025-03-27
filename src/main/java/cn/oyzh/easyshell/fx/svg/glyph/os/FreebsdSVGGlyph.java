package cn.oyzh.easyshell.fx.svg.glyph.os;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class FreebsdSVGGlyph extends SVGGlyph {

    public FreebsdSVGGlyph() {
        super("/font/os/freebsd.svg");
    }

    public FreebsdSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
