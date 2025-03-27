package cn.oyzh.easyshell.fx.svg.glyph.file;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class FileJarSVGGlyph extends SVGGlyph {

    public FileJarSVGGlyph() {
        super("/font/file-jar.svg");
    }

    public FileJarSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
