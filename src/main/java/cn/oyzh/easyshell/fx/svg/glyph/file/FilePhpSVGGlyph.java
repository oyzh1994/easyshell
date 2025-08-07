package cn.oyzh.easyshell.fx.svg.glyph.file;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class FilePhpSVGGlyph extends SVGGlyph {

    public FilePhpSVGGlyph() {
        super("/font/file/file-php.svg");
    }

    public FilePhpSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
