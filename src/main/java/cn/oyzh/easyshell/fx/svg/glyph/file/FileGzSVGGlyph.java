package cn.oyzh.easyshell.fx.svg.glyph.file;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class FileGzSVGGlyph extends SVGGlyph {

    public FileGzSVGGlyph() {
        super("/font/file-gzip.svg");
    }

    public FileGzSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
