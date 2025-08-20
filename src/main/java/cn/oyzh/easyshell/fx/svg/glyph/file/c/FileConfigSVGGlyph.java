package cn.oyzh.easyshell.fx.svg.glyph.file.c;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class FileConfigSVGGlyph extends SVGGlyph {

    public FileConfigSVGGlyph() {
        super("/font/file/c/file-config.svg");
    }

    public FileConfigSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
