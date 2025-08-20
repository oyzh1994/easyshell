package cn.oyzh.easyshell.fx.svg.glyph.file.a;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class FileAacSVGGlyph extends SVGGlyph {

    public FileAacSVGGlyph() {
        super("/font/file/a/file-aac.svg");
    }

    public FileAacSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
