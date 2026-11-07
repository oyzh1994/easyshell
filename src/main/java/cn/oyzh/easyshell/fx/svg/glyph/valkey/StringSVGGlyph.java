package cn.oyzh.easyshell.fx.svg.glyph.valkey;

import cn.oyzh.fx.plus.controls.svg.ScalingSVGGlyph;

/**
 * Valkey String 类型图标
 *
 * @author oyzh
 * @since 2026-08-21
 */
public class StringSVGGlyph extends ScalingSVGGlyph {

    /**
     * 构造 Valkey String 类型图标
     */
    public StringSVGGlyph() {
        super("/font/valkey/string.svg");
        this.setStrokeWidth(1.8);
    }

    /**
     * 构造指定尺寸的 Valkey String 类型图标
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
