package cn.oyzh.easyshell.fx.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

public class CursorUnderlineSVGGlyph extends SVGGlyph {

    public CursorUnderlineSVGGlyph() {
        super("/font/cursor-underline.svg");
    }

    public CursorUnderlineSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
