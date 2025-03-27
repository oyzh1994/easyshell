package cn.oyzh.easyshell.fx.svg.glyph.file;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class FilePdfSVGGlyph extends SVGGlyph {

    public FilePdfSVGGlyph() {
        super("/font/file-pdf.svg");
    }

    public FilePdfSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
