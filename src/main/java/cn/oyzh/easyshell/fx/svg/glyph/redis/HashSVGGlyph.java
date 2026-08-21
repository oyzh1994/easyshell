package cn.oyzh.easyshell.fx.svg.glyph.redis;

import cn.oyzh.fx.plus.controls.svg.ScalingSVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class HashSVGGlyph extends ScalingSVGGlyph {

    public HashSVGGlyph() {
        super("/font/redis/hash.svg");
        this.setStrokeWidth(1.8);
    }

    public HashSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }

    @Override
    public double sizeScaling() {
        return 0.9;
    }
}
