package cn.oyzh.easyshell.fx.svg.glyph.redis;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-01-22
 */
public class KeysSVGGlyph extends SVGGlyph {

    public KeysSVGGlyph() {
        super("/font/key.svg");
    }

    public KeysSVGGlyph(String size) {
        this();
        super.setSizeStr(size);
    }
}
