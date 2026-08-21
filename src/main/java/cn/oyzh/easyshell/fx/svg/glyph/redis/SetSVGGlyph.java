package cn.oyzh.easyshell.fx.svg.glyph.redis;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class SetSVGGlyph extends SVGGlyph {

    public SetSVGGlyph() {
        super("/font/redis/set.svg");
        this.setStrokeWidth(1.8);
    }

    public SetSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
