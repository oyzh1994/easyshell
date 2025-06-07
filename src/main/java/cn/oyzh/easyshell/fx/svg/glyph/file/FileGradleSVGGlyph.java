package cn.oyzh.easyshell.fx.svg.glyph.file;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class FileGradleSVGGlyph extends SVGGlyph {

    public FileGradleSVGGlyph() {
        super("/font/file/file-gradle.svg");
    }

    public FileGradleSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
