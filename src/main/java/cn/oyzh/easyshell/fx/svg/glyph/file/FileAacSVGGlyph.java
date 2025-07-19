package cn.oyzh.easyshell.fx.svg.glyph.file;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class FileAacSVGGlyph extends SVGGlyph {

    public FileAacSVGGlyph() {
        super("/font/file/file-aac.svg");
    }

    public FileAacSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
