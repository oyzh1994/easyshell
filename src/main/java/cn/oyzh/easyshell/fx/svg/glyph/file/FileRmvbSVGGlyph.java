package cn.oyzh.easyshell.fx.svg.glyph.file;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class FileRmvbSVGGlyph extends SVGGlyph {

    public FileRmvbSVGGlyph() {
        super("/font/file/file-rmvb.svg");
    }

    public FileRmvbSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
