package cn.oyzh.easyshell.fx.svg.glyph.valkey;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * Valkey Stream 类型图标
 *
 * @author oyzh
 * @since 2025-06-29
 */
public class StreamSVGGlyph extends SVGGlyph {

    /**
     * 构造 Valkey Stream 类型图标
     */
    public StreamSVGGlyph() {
        super("/font/valkey/stream.svg");
        this.setStrokeWidth(1.8);
    }

    /**
     * 构造指定尺寸的 Valkey Stream 类型图标
     *
     * @param size 尺寸
     */
    public StreamSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
