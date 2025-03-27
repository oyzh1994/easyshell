package cn.oyzh.easyshell.fx.svg.glyph.os;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class UbuntuSVGGlyph extends SVGGlyph {

    public UbuntuSVGGlyph() {
        super("/font/os/ubuntu.svg");
    }

    public UbuntuSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
