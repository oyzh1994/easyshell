package cn.oyzh.easyshell.fx.svg.glyph.file.p;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class FilePlistSVGGlyph extends SVGGlyph {

    public FilePlistSVGGlyph() {
        super("/font/file/p/file-plist.svg");
    }

    public FilePlistSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
