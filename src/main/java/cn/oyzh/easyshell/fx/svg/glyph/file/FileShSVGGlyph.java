package cn.oyzh.easyshell.fx.svg.glyph.file;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class FileShSVGGlyph extends SVGGlyph {

    public FileShSVGGlyph() {
        super("/font/file/file-sh.svg");
    }

    public FileShSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
