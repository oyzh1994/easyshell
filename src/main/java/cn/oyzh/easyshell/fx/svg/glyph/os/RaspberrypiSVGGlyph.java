package cn.oyzh.easyshell.fx.svg.glyph.os;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-27
 */
public class RaspberrypiSVGGlyph extends SVGGlyph {

    public RaspberrypiSVGGlyph() {
        super("/font/os/raspberry-pi.svg");
    }

    public RaspberrypiSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
