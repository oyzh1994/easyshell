package cn.oyzh.easyshell.fx.svg.glyph.file;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class FileRtfSVGGlyph extends SVGGlyph {

    public FileRtfSVGGlyph() {
        super("/font/file/file-rtf.svg");
    }

    public FileRtfSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
