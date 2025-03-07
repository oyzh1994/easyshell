package cn.oyzh.easyssh.fx.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class FileCompressSVGGlyph extends SVGGlyph {

    public FileCompressSVGGlyph() {
        super("/font/file-compress.svg");
    }

    public FileCompressSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
