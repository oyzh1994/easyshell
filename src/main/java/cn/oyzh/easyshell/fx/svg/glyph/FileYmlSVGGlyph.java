package cn.oyzh.easyshell.fx.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class FileYmlSVGGlyph extends SVGGlyph {

    public FileYmlSVGGlyph() {
        super("/font/file-yml.svg");
    }

    public FileYmlSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
