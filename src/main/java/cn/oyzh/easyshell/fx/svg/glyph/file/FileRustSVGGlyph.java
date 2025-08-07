package cn.oyzh.easyshell.fx.svg.glyph.file;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class FileRustSVGGlyph extends SVGGlyph {

    public FileRustSVGGlyph() {
        super("/font/file/file-rust.svg");
    }

    public FileRustSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
