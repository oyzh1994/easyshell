package cn.oyzh.easyshell.fx.svg.glyph.file.w;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class FileWpsSVGGlyph extends SVGGlyph {

    public FileWpsSVGGlyph() {
        super("/font/file/w/file-wps.svg");
    }

    public FileWpsSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
