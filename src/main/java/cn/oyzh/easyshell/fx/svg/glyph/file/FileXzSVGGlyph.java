package cn.oyzh.easyshell.fx.svg.glyph.file;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class FileXzSVGGlyph extends SVGGlyph {

    public FileXzSVGGlyph() {
        super("/font/file/file-xz.svg");
    }

    public FileXzSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
