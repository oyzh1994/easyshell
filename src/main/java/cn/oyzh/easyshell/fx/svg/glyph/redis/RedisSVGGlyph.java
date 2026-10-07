package cn.oyzh.easyshell.fx.svg.glyph.redis;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * Redis 图标
 *
 * @author oyzh
 * @since 2025-06-29
 */
public class RedisSVGGlyph extends SVGGlyph {

    /**
     * 构造 Redis 图标
     */
    public RedisSVGGlyph() {
        super("/font/redis/redis.svg");
    }

    /**
     * 构造指定尺寸的 Redis 图标
     *
     * @param size 尺寸
     */
    public RedisSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
