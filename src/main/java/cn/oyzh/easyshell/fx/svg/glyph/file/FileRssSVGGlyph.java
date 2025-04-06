package cn.oyzh.easyshell.fx.svg.glyph.file;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-06
 */
public class FileRssSVGGlyph extends SVGGlyph {

    public FileRssSVGGlyph() {
        super("/font/file/file-rss.svg");
    }

    public FileRssSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
