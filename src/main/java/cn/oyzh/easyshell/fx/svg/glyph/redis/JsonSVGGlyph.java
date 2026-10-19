package cn.oyzh.easyshell.fx.svg.glyph.redis;

import cn.oyzh.fx.plus.controls.svg.ScalingSVGGlyph;

/**
 * Redis JSON 数据类型图标
 *
 * @author oyzh
 * @since 2026-08-21
 */
public class JsonSVGGlyph extends ScalingSVGGlyph {

    /**
     * 构造 Redis JSON 数据类型图标
     */
    public JsonSVGGlyph() {
        super("/font/redis/json.svg");
        this.setStrokeWidth(1.8);
    }

    /**
     * 构造指定尺寸的 Redis JSON 数据类型图标
     *
     * @param size 图标尺寸
     */
    public JsonSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }

    @Override
    public double sizeScaling() {
        return 0.85;
    }
}
