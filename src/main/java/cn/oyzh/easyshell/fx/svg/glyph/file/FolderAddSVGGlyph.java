package cn.oyzh.easyshell.fx.svg.glyph.file;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class FolderAddSVGGlyph extends SVGGlyph {

    public FolderAddSVGGlyph() {
        super("/font/file/folder-add.svg");
    }

    public FolderAddSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
