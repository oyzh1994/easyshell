package cn.oyzh.easyshell.fx.svg.glyph.file.g;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class FileGzSVGGlyph extends SVGGlyph {

    public FileGzSVGGlyph() {
        super("/font/file/g/file-gzip.svg");
    }

    public FileGzSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
