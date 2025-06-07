package cn.oyzh.easyshell.fx.svg.glyph.file;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class FileConfigSVGGlyph extends SVGGlyph {

    public FileConfigSVGGlyph() {
        super("/font/file/file-config.svg");
    }

    public FileConfigSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
