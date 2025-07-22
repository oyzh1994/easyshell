package cn.oyzh.easyshell.fx.svg.glyph.protocol;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class SMBSVGGlyph extends SVGGlyph {

    public SMBSVGGlyph() {
        super("/font/protocol/smb.svg");
    }

    public SMBSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
