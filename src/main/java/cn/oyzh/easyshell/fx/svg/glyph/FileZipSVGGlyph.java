package cn.oyzh.easyshell.fx.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class FileZipSVGGlyph extends SVGGlyph {

    public FileZipSVGGlyph() {
        super("/font/file-zip.svg");
    }

    public FileZipSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
