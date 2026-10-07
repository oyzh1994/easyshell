package cn.oyzh.easyshell.fx.svg.glyph.os;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * WindowsSVG图标
 *
 * @author oyzh
 * @since 2025-03-27
 */
public class WindowsSVGGlyph extends SVGGlyph {

    /**
     * 构造WindowsSVG图标
     */
    public WindowsSVGGlyph() {
        super("/font/os/windows.svg");
    }

    /**
     * 构造WindowsSVG图标
     *
     * @param size 图标尺寸
     */
    public WindowsSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
