package cn.oyzh.easyshell.fx.svg.glyph.file;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class FileAspxSVGGlyph extends SVGGlyph {

    public FileAspxSVGGlyph() {
        super("/font/file/file-aspx.svg");
    }

    public FileAspxSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
