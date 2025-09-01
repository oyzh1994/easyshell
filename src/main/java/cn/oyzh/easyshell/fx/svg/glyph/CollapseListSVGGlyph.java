package cn.oyzh.easyshell.fx.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

public class CollapseListSVGGlyph extends SVGGlyph {

    public CollapseListSVGGlyph() {
        super("/font/arrow-up-double-line.svg");
    }

    public CollapseListSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
