package cn.oyzh.easyshell.fx.svg.glyph.file.a;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class FileAspSVGGlyph extends SVGGlyph {

    public FileAspSVGGlyph() {
        super("/font/file/a/file-asp.svg");
    }

    public FileAspSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
