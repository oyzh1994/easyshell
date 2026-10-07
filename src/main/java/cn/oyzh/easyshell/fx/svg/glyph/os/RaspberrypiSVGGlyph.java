package cn.oyzh.easyshell.fx.svg.glyph.os;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * 树莓派SVG图标
 *
 * @author oyzh
 * @since 2025-03-27
 */
public class RaspberrypiSVGGlyph extends SVGGlyph {

    /**
     * 构造树莓派SVG图标
     */
    public RaspberrypiSVGGlyph() {
        super("/font/os/raspberry-pi.svg");
    }

    /**
     * 构造树莓派SVG图标
     *
     * @param size 图标尺寸
     */
    public RaspberrypiSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
