package cn.oyzh.easyshell.fx.svg.glyph.file;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class FileSvgSVGGlyph extends SVGGlyph {

    public FileSvgSVGGlyph() {
        super("/font/file/file-svg.svg");
    }

    public FileSvgSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
