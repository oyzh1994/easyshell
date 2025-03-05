package cn.oyzh.easyssh.fx.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class FileLinkSVGGlyph extends SVGGlyph {

    public FileLinkSVGGlyph() {
        super("/font/file-link.svg");
    }

    public FileLinkSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
