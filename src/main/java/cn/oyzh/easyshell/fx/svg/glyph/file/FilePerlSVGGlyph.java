package cn.oyzh.easyshell.fx.svg.glyph.file;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class FilePerlSVGGlyph extends SVGGlyph {

    public FilePerlSVGGlyph() {
        super("/font/file/file-perl.svg");
    }

    public FilePerlSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
