package cn.oyzh.easyshell.fx.svg.glyph.file.d;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class FileDbSVGGlyph extends SVGGlyph {

    public FileDbSVGGlyph() {
        super("/font/file/d/file-db.svg");
    }

    public FileDbSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
