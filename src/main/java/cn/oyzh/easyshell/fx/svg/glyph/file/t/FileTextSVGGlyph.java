package cn.oyzh.easyshell.fx.svg.glyph.file.t;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class FileTextSVGGlyph extends SVGGlyph {

    public FileTextSVGGlyph() {
        super("/font/file/t/file-text.svg");
    }

    public FileTextSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
