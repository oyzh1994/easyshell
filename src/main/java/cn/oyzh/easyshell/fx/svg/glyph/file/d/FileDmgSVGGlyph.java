package cn.oyzh.easyshell.fx.svg.glyph.file.d;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-06
 */
public class FileDmgSVGGlyph extends SVGGlyph {

    public FileDmgSVGGlyph() {
        super("/font/file/d/file-dmg.svg");
    }

    public FileDmgSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
