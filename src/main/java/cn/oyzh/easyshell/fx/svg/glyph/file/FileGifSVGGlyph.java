package cn.oyzh.easyshell.fx.svg.glyph.file;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class FileGifSVGGlyph extends SVGGlyph {

    public FileGifSVGGlyph() {
        super("/font/file/file-gif.svg");
    }

    public FileGifSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
