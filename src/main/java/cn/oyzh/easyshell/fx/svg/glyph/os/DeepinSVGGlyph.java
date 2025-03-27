package cn.oyzh.easyshell.fx.svg.glyph.os;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class DeepinSVGGlyph extends SVGGlyph {

    public DeepinSVGGlyph() {
        super("/font/os/deepin.svg");
    }

    public DeepinSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
