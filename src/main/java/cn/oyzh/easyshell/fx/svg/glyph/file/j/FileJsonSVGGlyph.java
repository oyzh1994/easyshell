package cn.oyzh.easyshell.fx.svg.glyph.file.j;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class FileJsonSVGGlyph extends SVGGlyph {

    public FileJsonSVGGlyph() {
        super("/font/file/j/file-json.svg");
    }

    public FileJsonSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
