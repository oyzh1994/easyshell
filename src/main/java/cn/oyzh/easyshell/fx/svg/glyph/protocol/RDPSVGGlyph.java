package cn.oyzh.easyshell.fx.svg.glyph.protocol;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class RDPSVGGlyph extends SVGGlyph {

    public RDPSVGGlyph() {
        super("/font/protocol/rdp.svg");
    }

    public RDPSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
