package cn.oyzh.easyshell.fx.svg.glyph.file;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class FileVbsSVGGlyph extends SVGGlyph {

    public FileVbsSVGGlyph() {
        super("/font/file/file-vbs.svg");
    }

    public FileVbsSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
