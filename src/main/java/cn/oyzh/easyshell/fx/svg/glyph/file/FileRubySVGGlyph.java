package cn.oyzh.easyshell.fx.svg.glyph.file;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class FileRubySVGGlyph extends SVGGlyph {

    public FileRubySVGGlyph() {
        super("/font/file/file-ruby.svg");
    }

    public FileRubySVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
