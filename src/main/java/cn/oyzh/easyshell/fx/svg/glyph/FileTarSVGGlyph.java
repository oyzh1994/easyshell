package cn.oyzh.easyshell.fx.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class FileTarSVGGlyph extends SVGGlyph {

    public FileTarSVGGlyph() {
        super("/font/file-tar.svg");
    }

    public FileTarSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
