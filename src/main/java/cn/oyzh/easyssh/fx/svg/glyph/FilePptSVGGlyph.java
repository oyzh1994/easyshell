package cn.oyzh.easyssh.fx.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class FilePptSVGGlyph extends SVGGlyph {

    public FilePptSVGGlyph() {
        super("/font/file-ppt.svg");
    }

    public FilePptSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
