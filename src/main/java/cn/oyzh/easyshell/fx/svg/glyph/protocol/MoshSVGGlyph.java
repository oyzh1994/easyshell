package cn.oyzh.easyshell.fx.svg.glyph.protocol;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class MoshSVGGlyph extends SVGGlyph {

    public MoshSVGGlyph() {
        super("/font/protocol/mosh.svg");
    }

    public MoshSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
