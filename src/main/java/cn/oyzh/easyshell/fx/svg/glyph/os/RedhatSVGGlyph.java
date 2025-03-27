package cn.oyzh.easyshell.fx.svg.glyph.os;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class RedhatSVGGlyph extends SVGGlyph {

    public RedhatSVGGlyph() {
        super("/font/os/redhat.svg");
    }

    public RedhatSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
