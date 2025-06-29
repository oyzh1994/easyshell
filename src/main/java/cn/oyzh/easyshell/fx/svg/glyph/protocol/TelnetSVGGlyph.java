package cn.oyzh.easyshell.fx.svg.glyph.protocol;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class TelnetSVGGlyph extends SVGGlyph {

    public TelnetSVGGlyph() {
        super("/font/protocol/telnet.svg");
    }

    public TelnetSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
