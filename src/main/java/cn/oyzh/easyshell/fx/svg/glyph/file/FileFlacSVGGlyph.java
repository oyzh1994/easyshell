package cn.oyzh.easyshell.fx.svg.glyph.file;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class FileFlacSVGGlyph extends SVGGlyph {

    public FileFlacSVGGlyph() {
        super("/font/file/file-flac.svg");
    }

    public FileFlacSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
