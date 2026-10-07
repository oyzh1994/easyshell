package cn.oyzh.easyshell.fx.svg.glyph.os;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * 红帽SVG图标
 *
 * @author oyzh
 * @since 2025-03-05
 */
public class RedhatSVGGlyph extends SVGGlyph {

    /**
     * 构造红帽SVG图标
     */
    public RedhatSVGGlyph() {
        super("/font/os/redhat.svg");
    }

    /**
     * 构造红帽SVG图标
     *
     * @param size 图标尺寸
     */
    public RedhatSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
