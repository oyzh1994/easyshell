package cn.oyzh.easyshell.fx.svg.glyph.file;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class FolderSVGGlyph extends SVGGlyph {

    public FolderSVGGlyph() {
        super("/font/file/folder.svg");
    }

    public FolderSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
