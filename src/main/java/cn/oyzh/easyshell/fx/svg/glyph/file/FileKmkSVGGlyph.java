package cn.oyzh.easyshell.fx.svg.glyph.file;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class FileKmkSVGGlyph extends SVGGlyph {

    public FileKmkSVGGlyph() {
        super("/font/file/file-kmk.svg");
    }

    public FileKmkSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
