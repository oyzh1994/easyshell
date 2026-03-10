package cn.oyzh.easyshell.fx.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

public class CursorBarSVGGlyph extends SVGGlyph {

    public CursorBarSVGGlyph() {
        super("/font/cursor-bar.svg");
    }

    public CursorBarSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
