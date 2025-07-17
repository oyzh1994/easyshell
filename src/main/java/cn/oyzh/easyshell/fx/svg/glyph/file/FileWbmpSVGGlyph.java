package cn.oyzh.easyshell.fx.svg.glyph.file;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class FileWbmpSVGGlyph extends SVGGlyph {

    public FileWbmpSVGGlyph() {
        super("/font/file/file-wbmp.svg");
    }

    public FileWbmpSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
