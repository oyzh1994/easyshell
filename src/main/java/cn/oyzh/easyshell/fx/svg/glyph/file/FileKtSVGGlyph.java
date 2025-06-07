package cn.oyzh.easyshell.fx.svg.glyph.file;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class FileKtSVGGlyph extends SVGGlyph {

    public FileKtSVGGlyph() {
        super("/font/file/file-kt.svg");
    }

    public FileKtSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
