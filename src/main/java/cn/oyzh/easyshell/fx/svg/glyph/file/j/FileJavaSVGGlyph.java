package cn.oyzh.easyshell.fx.svg.glyph.file.j;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class FileJavaSVGGlyph extends SVGGlyph {

    public FileJavaSVGGlyph() {
        super("/font/file/j/file-java.svg");
    }

    public FileJavaSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
