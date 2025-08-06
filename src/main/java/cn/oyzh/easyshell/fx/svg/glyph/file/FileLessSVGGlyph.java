package cn.oyzh.easyshell.fx.svg.glyph.file;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class FileLessSVGGlyph extends SVGGlyph {

    public FileLessSVGGlyph() {
        super("/font/file/file-less.svg");
    }

    public FileLessSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
