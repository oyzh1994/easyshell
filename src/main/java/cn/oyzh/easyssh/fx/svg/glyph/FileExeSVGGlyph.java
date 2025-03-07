package cn.oyzh.easyssh.fx.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-06
 */
public class FileExeSVGGlyph extends SVGGlyph {

    public FileExeSVGGlyph() {
        super("/font/file-exe.svg");
    }

    public FileExeSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
