package cn.oyzh.easyshell.fx.svg.glyph.file.e;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class FileEpubSVGGlyph extends SVGGlyph {

    public FileEpubSVGGlyph() {
        super("/font/file/e/file-epub.svg");
    }

    public FileEpubSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
