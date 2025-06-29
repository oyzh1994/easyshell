package cn.oyzh.easyshell.fx.svg.glyph.protocol;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class SFTPSVGGlyph extends SVGGlyph {

    public SFTPSVGGlyph() {
        super("/font/protocol/sftp.svg");
    }

    public SFTPSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
