package cn.oyzh.easyshell.fx.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * 发布SVG图标
 *
 * @author oyzh
 * @since 2024-10-16
 */
public class PublishSVGGlyph extends SVGGlyph {

    /**
     * 构造发布SVG图标
     */
    public PublishSVGGlyph() {
        super("/font/publish.svg");
    }

    /**
     * 构造发布SVG图标
     *
     * @param size 图标尺寸
     */
    public PublishSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
