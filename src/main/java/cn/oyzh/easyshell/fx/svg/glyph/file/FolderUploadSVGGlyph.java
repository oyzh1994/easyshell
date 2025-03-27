package cn.oyzh.easyshell.fx.svg.glyph.file;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-06
 */
public class FolderUploadSVGGlyph extends SVGGlyph {

    public FolderUploadSVGGlyph() {
        super("/font/folder-upload.svg");
    }

    public FolderUploadSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
