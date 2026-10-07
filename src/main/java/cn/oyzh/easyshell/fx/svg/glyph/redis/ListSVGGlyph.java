package cn.oyzh.easyshell.fx.svg.glyph.redis;

import cn.oyzh.fx.plus.controls.svg.ScalingSVGGlyph;

/**
 * Redis List 数据类型图标
 *
 * @author oyzh
 * @since 2026-08-21
 */
public class ListSVGGlyph extends ScalingSVGGlyph {

    /**
     * 构造 Redis List 数据类型图标
     */
    public ListSVGGlyph() {
        super("/font/redis/list.svg");
        this.setStrokeWidth(1.8);
    }

    /**
     * 构造指定尺寸的 Redis List 数据类型图标
     *
     * @param size 图标尺寸
     */
    public ListSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }

    @Override
    public double heightScaling() {
        return 0.75;
    }
}
