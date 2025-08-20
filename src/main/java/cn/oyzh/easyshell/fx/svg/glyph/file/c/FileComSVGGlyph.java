package cn.oyzh.easyshell.fx.svg.glyph.file.c;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class FileComSVGGlyph extends SVGGlyph {

    public FileComSVGGlyph() {
        super("/font/file/c/file-com.svg");
    }

    public FileComSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
