package cn.oyzh.easyshell.fx.svg.glyph.redis;

import cn.oyzh.fx.plus.controls.svg.ScalingSVGGlyph;

/**
 * Redis String 类型图标
 *
 * @author oyzh
 * @since 2025-03-05
 */
public class StringSVGGlyph extends ScalingSVGGlyph {

    /**
     * 构造 Redis String 类型图标
     */
    public StringSVGGlyph() {
        super("/font/redis/string.svg");
        this.setStrokeWidth(1.8);
    }

    /**
     * 构造指定尺寸的 Redis String 类型图标
     *
     * @param size 尺寸
     */
    public StringSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }

    @Override
    public double sizeScaling() {
        return 0.8;
    }
}
