package cn.oyzh.easyshell.fx.svg.glyph.os;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * UbuntuSVG图标
 *
 * @author oyzh
 * @since 2025-03-05
 */
public class UbuntuSVGGlyph extends SVGGlyph {

    /**
     * 构造UbuntuSVG图标
     */
    public UbuntuSVGGlyph() {
        super("/font/os/ubuntu.svg");
    }

    /**
     * 构造UbuntuSVG图标
     *
     * @param size 图标尺寸
     */
    public UbuntuSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
