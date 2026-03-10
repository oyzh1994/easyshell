package cn.oyzh.easyshell.fx.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

public class CursorBlockSVGGlyph extends SVGGlyph {

    public CursorBlockSVGGlyph() {
        super("/font/cursor-block.svg");
    }

    public CursorBlockSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
