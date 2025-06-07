package cn.oyzh.easyshell.fx.svg.glyph.file;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class FileFlvSVGGlyph extends SVGGlyph {

    public FileFlvSVGGlyph() {
        super("/font/file/file-flv.svg");
    }

    public FileFlvSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
