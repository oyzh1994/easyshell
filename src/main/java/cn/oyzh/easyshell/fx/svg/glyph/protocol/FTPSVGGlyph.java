package cn.oyzh.easyshell.fx.svg.glyph.protocol;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class FTPSVGGlyph extends SVGGlyph {

    public FTPSVGGlyph() {
        super("/font/protocol/ftp.svg");
    }

    public FTPSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
