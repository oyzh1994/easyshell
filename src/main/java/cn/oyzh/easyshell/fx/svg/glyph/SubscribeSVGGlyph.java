package cn.oyzh.easyshell.fx.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * 订阅SVG图标
 *
 * @author oyzh
 * @since 2024-10-16
 */
public class SubscribeSVGGlyph extends SVGGlyph {

    /**
     * 构造订阅SVG图标
     */
    public SubscribeSVGGlyph() {
        super("/font/subscribe.svg");
    }

    /**
     * 构造订阅SVG图标
     *
     * @param size 图标尺寸
     */
    public SubscribeSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
