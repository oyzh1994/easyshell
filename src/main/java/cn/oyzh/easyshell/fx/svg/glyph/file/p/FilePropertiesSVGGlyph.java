package cn.oyzh.easyshell.fx.svg.glyph.file.p;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class FilePropertiesSVGGlyph extends SVGGlyph {

    public FilePropertiesSVGGlyph() {
        super("/font/file/p/file-properties.svg");
    }

    public FilePropertiesSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
