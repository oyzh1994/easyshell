package cn.oyzh.easyshell.fx.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class FileBinSVGGlyph extends SVGGlyph {

    public FileBinSVGGlyph() {
        super("/font/file-bin.svg");
    }

    public FileBinSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
