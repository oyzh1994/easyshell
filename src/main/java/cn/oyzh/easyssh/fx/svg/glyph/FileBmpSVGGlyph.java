package cn.oyzh.easyssh.fx.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class FileBmpSVGGlyph extends SVGGlyph {

    public FileBmpSVGGlyph() {
        super("/font/file-bmp.svg");
    }

    public FileBmpSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
