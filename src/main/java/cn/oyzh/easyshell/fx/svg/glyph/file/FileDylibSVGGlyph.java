package cn.oyzh.easyshell.fx.svg.glyph.file;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-06
 */
public class FileDylibSVGGlyph extends SVGGlyph {

    public FileDylibSVGGlyph() {
        super("/font/file/file-dylib.svg");
    }

    public FileDylibSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
