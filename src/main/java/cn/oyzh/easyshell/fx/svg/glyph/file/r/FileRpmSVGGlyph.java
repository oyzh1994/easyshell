package cn.oyzh.easyshell.fx.svg.glyph.file.r;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class FileRpmSVGGlyph extends SVGGlyph {

    public FileRpmSVGGlyph() {
        super("/font/file/r/file-rpm.svg");
    }

    public FileRpmSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
