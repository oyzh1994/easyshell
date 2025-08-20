package cn.oyzh.easyshell.fx.svg.glyph.file.t;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class FileTexSVGGlyph extends SVGGlyph {

    public FileTexSVGGlyph() {
        super("/font/file/t/file-tex.svg");
    }

    public FileTexSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
