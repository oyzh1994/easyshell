package cn.oyzh.easyshell.fx.svg.glyph.file.s;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class FileSasSVGGlyph extends SVGGlyph {

    public FileSasSVGGlyph() {
        super("/font/file/s/file-sas.svg");
    }

    public FileSasSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
