package cn.oyzh.easyshell.fx.svg.glyph.file.m;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class FileMarkdownSVGGlyph extends SVGGlyph {

    public FileMarkdownSVGGlyph() {
        super("/font/file/m/file-markdown.svg");
    }

    public FileMarkdownSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
