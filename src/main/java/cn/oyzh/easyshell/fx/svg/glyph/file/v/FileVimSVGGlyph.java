package cn.oyzh.easyshell.fx.svg.glyph.file.v;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class FileVimSVGGlyph extends SVGGlyph {

    public FileVimSVGGlyph() {
        super("/font/file/v/file-vim.svg");
    }

    public FileVimSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
