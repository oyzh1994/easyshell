package cn.oyzh.easyshell.fx.svg.glyph.os;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class MintSVGGlyph extends SVGGlyph {

    public MintSVGGlyph() {
        super("/font/os/mint.svg");
    }

    public MintSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
