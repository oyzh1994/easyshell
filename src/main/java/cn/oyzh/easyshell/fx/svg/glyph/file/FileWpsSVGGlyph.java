package cn.oyzh.easyshell.fx.svg.glyph.file;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class FileWpsSVGGlyph extends SVGGlyph {

    public FileWpsSVGGlyph() {
        super("/font/file/file-wps.svg");
    }

    public FileWpsSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
