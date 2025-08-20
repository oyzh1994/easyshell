package cn.oyzh.easyshell.fx.svg.glyph.file.o;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class FileOcxSVGGlyph extends SVGGlyph {

    public FileOcxSVGGlyph() {
        super("/font/file/o/file-ocx.svg");
    }

    public FileOcxSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
