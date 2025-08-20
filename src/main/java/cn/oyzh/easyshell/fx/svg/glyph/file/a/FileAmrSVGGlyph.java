package cn.oyzh.easyshell.fx.svg.glyph.file.a;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class FileAmrSVGGlyph extends SVGGlyph {

    public FileAmrSVGGlyph() {
        super("/font/file/a/file-amr.svg");
    }

    public FileAmrSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
