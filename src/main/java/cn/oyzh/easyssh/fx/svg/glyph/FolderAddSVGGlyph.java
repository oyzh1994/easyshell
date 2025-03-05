package cn.oyzh.easyssh.fx.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class FolderAddSVGGlyph extends SVGGlyph {

    public FolderAddSVGGlyph() {
        super("/font/folder-add.svg");
    }

    public FolderAddSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
