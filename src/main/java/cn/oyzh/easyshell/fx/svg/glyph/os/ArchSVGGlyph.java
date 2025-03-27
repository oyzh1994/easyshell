package cn.oyzh.easyshell.fx.svg.glyph.os;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class ArchSVGGlyph extends SVGGlyph {

    public ArchSVGGlyph() {
        super("/font/os/arch.svg");
    }

    public ArchSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
