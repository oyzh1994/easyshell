package cn.oyzh.easyshell.fx.svg.glyph.zk;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-01-23
 */
public class TempSVGGlyph extends SVGGlyph {

    public TempSVGGlyph() {
        super("/font/zk/temp.svg");
    }

    public TempSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
