package cn.oyzh.easyshell.fx.svg.glyph.file;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class FileIcoSVGGlyph extends SVGGlyph {

    public FileIcoSVGGlyph() {
        super("/font/file/file-ico.svg");
    }

    public FileIcoSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
