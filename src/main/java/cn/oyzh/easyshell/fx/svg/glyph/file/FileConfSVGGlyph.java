package cn.oyzh.easyshell.fx.svg.glyph.file;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class FileConfSVGGlyph extends SVGGlyph {

    public FileConfSVGGlyph() {
        super("/font/file-conf.svg");
    }

    public FileConfSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
