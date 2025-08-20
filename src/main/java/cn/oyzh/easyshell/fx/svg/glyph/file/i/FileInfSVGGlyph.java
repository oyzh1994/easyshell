package cn.oyzh.easyshell.fx.svg.glyph.file.i;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class FileInfSVGGlyph extends SVGGlyph {

    public FileInfSVGGlyph() {
        super("/font/file/i/file-inf.svg");
    }

    public FileInfSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
