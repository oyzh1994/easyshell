package cn.oyzh.easyshell.fx.svg.glyph.file.d;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class FileDartSVGGlyph extends SVGGlyph {

    public FileDartSVGGlyph() {
        super("/font/file/d/file-dart.svg");
    }

    public FileDartSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
