package cn.oyzh.easyshell.fx.svg.glyph.file;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class FileXlsSVGGlyph extends SVGGlyph {

    public FileXlsSVGGlyph() {
        super("/font/file/file-xls.svg");
    }

    public FileXlsSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
