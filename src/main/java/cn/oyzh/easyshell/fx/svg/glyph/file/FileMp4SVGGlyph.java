package cn.oyzh.easyshell.fx.svg.glyph.file;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-06
 */
public class FileMp4SVGGlyph extends SVGGlyph {

    public FileMp4SVGGlyph() {
        super("/font/file/file-mp4.svg");
    }

    public FileMp4SVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
