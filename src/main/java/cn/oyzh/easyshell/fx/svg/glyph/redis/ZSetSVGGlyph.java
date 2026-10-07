package cn.oyzh.easyshell.fx.svg.glyph.redis;

import cn.oyzh.fx.plus.controls.svg.ScalingSVGGlyph;

/**
 * Redis ZSet 类型图标
 *
 * @author oyzh
 * @since 2025-03-05
 */
public class ZSetSVGGlyph extends ScalingSVGGlyph {

    /**
     * 构造 Redis ZSet 类型图标
     */
    public ZSetSVGGlyph() {
        super("/font/redis/zset.svg");
        this.setStrokeWidth(1.8);
    }

    /**
     * 构造指定尺寸的 Redis ZSet 类型图标
     *
     * @param size 尺寸
     */
    public ZSetSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }

    @Override
    public double heightScaling() {
        return 0.8;
    }
}
