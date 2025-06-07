package cn.oyzh.easyshell.fx.svg.glyph.file;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class FileLuaSVGGlyph extends SVGGlyph {

    public FileLuaSVGGlyph() {
        super("/font/file/file-vb.svg");
    }

    public FileLuaSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
