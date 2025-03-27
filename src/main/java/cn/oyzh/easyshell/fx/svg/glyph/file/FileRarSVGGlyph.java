package cn.oyzh.easyshell.fx.svg.glyph.file;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class FileRarSVGGlyph extends SVGGlyph {

    public FileRarSVGGlyph() {
        super("/font/file-rar.svg");
    }

    public FileRarSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
