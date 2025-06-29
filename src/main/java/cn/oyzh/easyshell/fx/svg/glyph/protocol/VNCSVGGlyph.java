package cn.oyzh.easyshell.fx.svg.glyph.protocol;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class VNCSVGGlyph extends SVGGlyph {

    public VNCSVGGlyph() {
        super("/font/protocol/vnc.svg");
    }

    public VNCSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
