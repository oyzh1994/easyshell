package cn.oyzh.easyshell.fx.svg.glyph.file.c;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class FileCerSVGGlyph extends SVGGlyph {

    public FileCerSVGGlyph() {
        super("/font/file/c/file-cer.svg");
    }

    public FileCerSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
