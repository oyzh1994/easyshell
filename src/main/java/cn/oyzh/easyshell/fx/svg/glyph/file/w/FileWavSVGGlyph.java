package cn.oyzh.easyshell.fx.svg.glyph.file.w;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class FileWavSVGGlyph extends SVGGlyph {

    public FileWavSVGGlyph() {
        super("/font/file/w/file-wav.svg");
    }

    public FileWavSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
