package cn.oyzh.easyshell.fx.svg.glyph.file;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class FileHtmlSVGGlyph extends SVGGlyph {

    public FileHtmlSVGGlyph() {
        super("/font/file/file-html.svg");
    }

    public FileHtmlSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
