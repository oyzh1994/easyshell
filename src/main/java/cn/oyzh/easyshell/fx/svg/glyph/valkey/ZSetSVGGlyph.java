package cn.oyzh.easyshell.fx.svg.glyph.valkey;

import cn.oyzh.fx.plus.controls.svg.ScalingSVGGlyph;

/**
 * Valkey ZSet 类型图标
 *
 * @author oyzh
 * @since 2026-08-21
 */
public class ZSetSVGGlyph extends ScalingSVGGlyph {

    /**
     * 构造 Valkey ZSet 类型图标
     */
    public ZSetSVGGlyph() {
        super("/font/valkey/zset.svg");
        this.setStrokeWidth(1.8);
    }

    /**
     * 构造指定尺寸的 Valkey ZSet 类型图标
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
