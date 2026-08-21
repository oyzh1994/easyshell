package cn.oyzh.easyshell.fx.svg.glyph.redis;

import cn.oyzh.fx.plus.controls.svg.ScalingSVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class ZSetSVGGlyph extends ScalingSVGGlyph {

    public ZSetSVGGlyph() {
        super("/font/redis/zset.svg");
        this.setStrokeWidth(1.8);
    }

    public ZSetSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }

    @Override
    public double heightScaling() {
        return 0.8;
    }
}
