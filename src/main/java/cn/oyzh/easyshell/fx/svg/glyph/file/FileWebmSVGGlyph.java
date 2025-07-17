package cn.oyzh.easyshell.fx.svg.glyph.file;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class FileWebmSVGGlyph extends SVGGlyph {

    public FileWebmSVGGlyph() {
        super("/font/file/file-webm.svg");
    }

    public FileWebmSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
