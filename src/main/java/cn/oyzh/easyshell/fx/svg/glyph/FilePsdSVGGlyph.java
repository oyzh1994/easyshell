package cn.oyzh.easyshell.fx.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class FilePsdSVGGlyph extends SVGGlyph {

    public FilePsdSVGGlyph() {
        super("/font/file-psd.svg");
    }

    public FilePsdSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
