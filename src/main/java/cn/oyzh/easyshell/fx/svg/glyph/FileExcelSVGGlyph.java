package cn.oyzh.easyshell.fx.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class FileExcelSVGGlyph extends SVGGlyph {

    public FileExcelSVGGlyph() {
        super("/font/file-excel.svg");
    }

    public FileExcelSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
