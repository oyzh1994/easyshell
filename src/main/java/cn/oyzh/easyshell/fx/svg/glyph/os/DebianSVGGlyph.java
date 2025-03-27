package cn.oyzh.easyshell.fx.svg.glyph.os;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class DebianSVGGlyph extends SVGGlyph {

    public DebianSVGGlyph() {
        super("/font/os/debian.svg");
    }

    public DebianSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
