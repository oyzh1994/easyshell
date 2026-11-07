package cn.oyzh.easyshell.fx.svg.glyph.valkey;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * Valkey 键图标
 *
 * @author oyzh
 * @since 2025-09-01
 */
public class KeysSVGGlyph extends SVGGlyph {

    /**
     * 构造 Valkey 键图标
     */
    public KeysSVGGlyph() {
        super("/font/valkey/keys.svg");
    }

    /**
     * 构造指定尺寸的 Valkey 键图标
     *
     * @param size 图标尺寸
     */
    public KeysSVGGlyph(String size) {
        this();
        super.setSizeStr(size);
    }
}
