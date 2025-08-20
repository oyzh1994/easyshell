package cn.oyzh.easyshell.fx.svg.glyph.file.o;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class FileOggSVGGlyph extends SVGGlyph {

    public FileOggSVGGlyph() {
        super("/font/file/o/file-ogg.svg");
    }

    public FileOggSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
