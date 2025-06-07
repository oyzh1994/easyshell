package cn.oyzh.easyshell.fx.svg.glyph.file;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class FileWarSVGGlyph extends SVGGlyph {

    public FileWarSVGGlyph() {
        super("/font/file/file-war.svg");
    }

    public FileWarSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
