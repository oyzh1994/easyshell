package cn.oyzh.easyshell.fx.svg.glyph.file;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class FilePySVGGlyph extends SVGGlyph {

    public FilePySVGGlyph() {
        super("/font/file-py.svg");
    }

    public FilePySVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
