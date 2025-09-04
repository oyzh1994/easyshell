package cn.oyzh.easyshell.fx.svg.glyph.redis;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class RedisSVGGlyph extends SVGGlyph {

    public RedisSVGGlyph() {
        super("/font/redis/redis.svg");
    }

    public RedisSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
