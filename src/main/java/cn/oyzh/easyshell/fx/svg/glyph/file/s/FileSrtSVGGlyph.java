package cn.oyzh.easyshell.fx.svg.glyph.file.s;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-06
 */
public class FileSrtSVGGlyph extends SVGGlyph {

    public FileSrtSVGGlyph() {
        super("/font/file/s/file-srt.svg");
    }

    public FileSrtSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
