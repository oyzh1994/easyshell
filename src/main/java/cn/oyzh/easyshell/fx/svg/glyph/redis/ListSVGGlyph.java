package cn.oyzh.easyshell.fx.svg.glyph.redis;

import cn.oyzh.fx.plus.controls.svg.ScalingSVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class ListSVGGlyph extends ScalingSVGGlyph {

    public ListSVGGlyph() {
        super("/font/redis/list.svg");
        this.setStrokeWidth(1.8);
    }

    public ListSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }

    @Override
    public double heightScaling() {
        return 0.75;
    }
}
