package cn.oyzh.easyshell.fx.svg.glyph.file.p;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class FilePugSVGGlyph extends SVGGlyph {

    public FilePugSVGGlyph() {
        super("/font/file/p/file-pug.svg");
    }

    public FilePugSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
