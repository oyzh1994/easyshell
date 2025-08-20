package cn.oyzh.easyshell.fx.svg.glyph.file.b;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class FileBmpSVGGlyph extends SVGGlyph {

    public FileBmpSVGGlyph() {
        super("/font/file/b/file-bmp.svg");
    }

    public FileBmpSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
