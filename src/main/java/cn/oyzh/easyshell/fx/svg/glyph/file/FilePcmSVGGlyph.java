package cn.oyzh.easyshell.fx.svg.glyph.file;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class FilePcmSVGGlyph extends SVGGlyph {

    public FilePcmSVGGlyph() {
        super("/font/file/file-pcm.svg");
    }

    public FilePcmSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
