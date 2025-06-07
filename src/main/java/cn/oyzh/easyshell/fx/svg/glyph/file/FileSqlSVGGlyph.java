package cn.oyzh.easyshell.fx.svg.glyph.file;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class FileSqlSVGGlyph extends SVGGlyph {

    public FileSqlSVGGlyph() {
        super("/font/file/file-sql.svg");
    }

    public FileSqlSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
