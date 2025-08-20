package cn.oyzh.easyshell.fx.svg.glyph.file.c;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class FileCfgSVGGlyph extends SVGGlyph {

    public FileCfgSVGGlyph() {
        super("/font/file/c/file-cfg.svg");
    }

    public FileCfgSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
