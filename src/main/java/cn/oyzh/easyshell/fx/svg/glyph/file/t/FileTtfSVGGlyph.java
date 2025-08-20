package cn.oyzh.easyshell.fx.svg.glyph.file.t;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class FileTtfSVGGlyph extends SVGGlyph {

    public FileTtfSVGGlyph() {
        super("/font/file/t/file-ttf.svg");
    }

    public FileTtfSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
