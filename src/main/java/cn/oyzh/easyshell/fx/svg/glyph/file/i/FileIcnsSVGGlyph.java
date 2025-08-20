package cn.oyzh.easyshell.fx.svg.glyph.file.i;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class FileIcnsSVGGlyph extends SVGGlyph {

    public FileIcnsSVGGlyph() {
        super("/font/file/i/file-icns.svg");
    }

    public FileIcnsSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
