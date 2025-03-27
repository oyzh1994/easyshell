package cn.oyzh.easyshell.fx.svg.glyph.file;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-06
 */
public class FileMp3SVGGlyph extends SVGGlyph {

    public FileMp3SVGGlyph() {
        super("/font/file-mp3.svg");
    }

    public FileMp3SVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
