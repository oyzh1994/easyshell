package cn.oyzh.easyshell.fx.svg.glyph.file;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class FolderLinkSVGGlyph extends SVGGlyph {

    public FolderLinkSVGGlyph() {
        super("/font/file/folder-link.svg");
    }

    public FolderLinkSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
