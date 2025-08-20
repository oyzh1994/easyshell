package cn.oyzh.easyshell.fx.svg.glyph.file.c;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class FileChmSVGGlyph extends SVGGlyph {

    public FileChmSVGGlyph() {
        super("/font/file/c/file-chm.svg");
    }

    public FileChmSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
