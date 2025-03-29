package cn.oyzh.easyshell.fx.svg.glyph.os;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class WindowsSVGGlyph extends SVGGlyph {

    public WindowsSVGGlyph() {
        super("/font/os/windows.svg");
    }

    public WindowsSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
