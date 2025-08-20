package cn.oyzh.easyshell.fx.svg.glyph.file.c;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class FileCsSVGGlyph extends SVGGlyph {

    public FileCsSVGGlyph() {
        super("/font/file/c/file-cs.svg");
    }

    public FileCsSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
