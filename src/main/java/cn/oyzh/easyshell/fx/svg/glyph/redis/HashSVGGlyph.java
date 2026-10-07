package cn.oyzh.easyshell.fx.svg.glyph.redis;

import cn.oyzh.fx.plus.controls.svg.ScalingSVGGlyph;

/**
 * Redis Hash 数据类型图标
 *
 * @author oyzh
 * @since 2025-03-05
 */
public class HashSVGGlyph extends ScalingSVGGlyph {

    /**
     * 构造 Redis Hash 数据类型图标
     */
    public HashSVGGlyph() {
        super("/font/redis/hash.svg");
        this.setStrokeWidth(1.8);
    }

    /**
     * 构造指定尺寸的 Redis Hash 数据类型图标
     *
     * @param size 图标尺寸
     */
    public HashSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }

    @Override
    public double sizeScaling() {
        return 0.9;
    }
}
