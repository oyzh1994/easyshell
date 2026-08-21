package cn.oyzh.easyshell.fx.svg.glyph.redis;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class StreamSVGGlyph extends SVGGlyph {

    public StreamSVGGlyph() {
        super("/font/redis/stream.svg");
        this.setStrokeWidth(1.8);
    }

    public StreamSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
