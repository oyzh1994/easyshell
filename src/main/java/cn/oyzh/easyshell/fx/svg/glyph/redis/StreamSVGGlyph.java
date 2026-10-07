package cn.oyzh.easyshell.fx.svg.glyph.redis;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * Redis Stream 类型图标
 *
 * @author oyzh
 * @since 2025-03-05
 */
public class StreamSVGGlyph extends SVGGlyph {

    /**
     * 构造 Redis Stream 类型图标
     */
    public StreamSVGGlyph() {
        super("/font/redis/stream.svg");
        this.setStrokeWidth(1.8);
    }

    /**
     * 构造指定尺寸的 Redis Stream 类型图标
     *
     * @param size 尺寸
     */
    public StreamSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
