package cn.oyzh.easyssh.fx.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class FileImageSVGGlyph extends SVGGlyph {

    public FileImageSVGGlyph() {
        super("/font/file-image.svg");
    }

    public FileImageSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
