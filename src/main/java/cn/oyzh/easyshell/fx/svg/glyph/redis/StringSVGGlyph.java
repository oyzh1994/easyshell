package cn.oyzh.easyshell.fx.svg.glyph.redis;

import cn.oyzh.fx.plus.controls.svg.ScalingSVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class StringSVGGlyph extends ScalingSVGGlyph {

    public StringSVGGlyph() {
        super("/font/redis/string.svg");
        this.setStrokeWidth(1.8);
    }

    public StringSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }

    @Override
    public double sizeScaling() {
        return 0.8;
    }
}
