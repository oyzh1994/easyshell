package cn.oyzh.easyshell.fx.svg.glyph.file.r;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class FileRtfSVGGlyph extends SVGGlyph {

    public FileRtfSVGGlyph() {
        super("/font/file/r/file-rtf.svg");
    }

    public FileRtfSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
