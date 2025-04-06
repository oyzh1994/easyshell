package cn.oyzh.easyshell.fx.svg.glyph.file;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-06
 */
public class FileIsoSVGGlyph extends SVGGlyph {

    public FileIsoSVGGlyph() {
        super("/font/file/file-iso.svg");
    }

    public FileIsoSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
