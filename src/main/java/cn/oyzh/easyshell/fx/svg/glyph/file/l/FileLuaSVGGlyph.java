package cn.oyzh.easyshell.fx.svg.glyph.file.l;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class FileLuaSVGGlyph extends SVGGlyph {

    public FileLuaSVGGlyph() {
        super("/font/file/l/file-lua.svg");
    }

    public FileLuaSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
