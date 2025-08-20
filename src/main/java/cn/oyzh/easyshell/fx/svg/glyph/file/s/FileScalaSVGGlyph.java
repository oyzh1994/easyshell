package cn.oyzh.easyshell.fx.svg.glyph.file.s;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class FileScalaSVGGlyph extends SVGGlyph {

    public FileScalaSVGGlyph() {
        super("/font/file/s/file-scala.svg");
    }

    public FileScalaSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
