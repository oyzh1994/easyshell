package cn.oyzh.easyssh.fx.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class FileJspSVGGlyph extends SVGGlyph {

    public FileJspSVGGlyph() {
        super("/font/file-jsp.svg");
    }

    public FileJspSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
