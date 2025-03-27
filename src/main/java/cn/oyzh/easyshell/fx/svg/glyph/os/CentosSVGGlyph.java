package cn.oyzh.easyshell.fx.svg.glyph.os;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class CentosSVGGlyph extends SVGGlyph {

    public CentosSVGGlyph() {
        super("/font/os/centos.svg");
    }

    public CentosSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
