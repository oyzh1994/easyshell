package cn.oyzh.easyshell.fx.svg.glyph.file;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-06
 */
public class FileDllSVGGlyph extends SVGGlyph {

    public FileDllSVGGlyph() {
        super("/font/file/file-dll.svg");
    }

    public FileDllSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
