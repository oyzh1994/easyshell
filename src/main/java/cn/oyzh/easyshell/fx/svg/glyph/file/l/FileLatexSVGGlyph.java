package cn.oyzh.easyshell.fx.svg.glyph.file.l;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class FileLatexSVGGlyph extends SVGGlyph {

    public FileLatexSVGGlyph() {
        super("/font/file/l/file-latex.svg");
    }

    public FileLatexSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
