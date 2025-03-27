package cn.oyzh.easyshell.fx.svg.glyph.file;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class FileSwfSVGGlyph extends SVGGlyph {

    public FileSwfSVGGlyph() {
        super("/font/file/file-swf.svg");
    }

    public FileSwfSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
