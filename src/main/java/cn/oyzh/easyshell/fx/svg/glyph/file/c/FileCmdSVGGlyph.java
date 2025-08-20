package cn.oyzh.easyshell.fx.svg.glyph.file.c;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class FileCmdSVGGlyph extends SVGGlyph {

    public FileCmdSVGGlyph() {
        super("/font/file/c/file-cmd.svg");
    }

    public FileCmdSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
