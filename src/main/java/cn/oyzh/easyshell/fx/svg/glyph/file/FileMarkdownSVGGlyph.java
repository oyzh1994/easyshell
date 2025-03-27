package cn.oyzh.easyshell.fx.svg.glyph.file;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class FileMarkdownSVGGlyph extends SVGGlyph {

    public FileMarkdownSVGGlyph() {
        super("/font/file/file-markdown.svg");
    }

    public FileMarkdownSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
