package cn.oyzh.easyshell.fx.svg.glyph.valkey;

import cn.oyzh.fx.plus.controls.svg.ScalingSVGGlyph;

/**
 * Valkey Hash 数据类型图标
 *
 * @author oyzh
 * @since 2026-08-21
 */
public class HashSVGGlyph extends ScalingSVGGlyph {

    /**
     * 构造 Valkey Hash 数据类型图标
     */
    public HashSVGGlyph() {
        super("/font/valkey/hash.svg");
        this.setStrokeWidth(1.8);
    }

    /**
     * 构造指定尺寸的 Valkey Hash 数据类型图标
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
