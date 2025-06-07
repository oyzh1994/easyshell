package cn.oyzh.easyshell.fx.svg.glyph.file;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class FileLinkSVGGlyph extends SVGGlyph {

    public FileLinkSVGGlyph() {
        super("/font/file/file-symlink-file.svg");
    }

    public FileLinkSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
