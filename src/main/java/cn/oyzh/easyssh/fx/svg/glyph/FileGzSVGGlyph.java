package cn.oyzh.easyssh.fx.svg.glyph;

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
