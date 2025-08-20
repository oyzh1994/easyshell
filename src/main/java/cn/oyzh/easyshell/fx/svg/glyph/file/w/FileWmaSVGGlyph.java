package cn.oyzh.easyshell.fx.svg.glyph.file.w;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class FileWmaSVGGlyph extends SVGGlyph {

    public FileWmaSVGGlyph() {
        super("/font/file/w/file-wma.svg");
    }

    public FileWmaSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
