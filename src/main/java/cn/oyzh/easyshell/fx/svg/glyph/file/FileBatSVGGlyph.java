package cn.oyzh.easyshell.fx.svg.glyph.file;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class FileBatSVGGlyph extends SVGGlyph {

    public FileBatSVGGlyph() {
        super("/font/file/file-bat.svg");
    }

    public FileBatSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
