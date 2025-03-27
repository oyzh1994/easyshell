package cn.oyzh.easyshell.fx.svg.glyph.file;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class FileIniSVGGlyph extends SVGGlyph {

    public FileIniSVGGlyph() {
        super("/font/file-ini.svg");
    }

    public FileIniSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
