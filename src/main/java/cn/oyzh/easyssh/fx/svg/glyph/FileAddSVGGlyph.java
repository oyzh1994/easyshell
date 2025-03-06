package cn.oyzh.easyssh.fx.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-06
 */
public class FileAddSVGGlyph extends SVGGlyph {

    public FileAddSVGGlyph() {
        super("/font/file-add.svg");
    }

    public FileAddSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
