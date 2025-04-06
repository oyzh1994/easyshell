package cn.oyzh.easyshell.fx.svg.glyph.file;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class FileRmSVGGlyph extends SVGGlyph {

    public FileRmSVGGlyph() {
        super("/font/file/file-rm.svg");
    }

    public FileRmSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
