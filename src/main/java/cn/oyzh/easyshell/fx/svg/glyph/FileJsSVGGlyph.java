package cn.oyzh.easyshell.fx.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class FileJsSVGGlyph extends SVGGlyph {

    public FileJsSVGGlyph() {
        super("/font/file-js.svg");
    }

    public FileJsSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
