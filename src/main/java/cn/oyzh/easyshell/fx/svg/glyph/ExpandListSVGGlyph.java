package cn.oyzh.easyshell.fx.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

public class ExpandListSVGGlyph extends SVGGlyph {

    public ExpandListSVGGlyph() {
        super("/font/arrow-down-double-line.svg");
    }

    public ExpandListSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
