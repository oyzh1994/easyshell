package cn.oyzh.easyshell.fx.svg.glyph.file;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class FileYamlSVGGlyph extends SVGGlyph {

    public FileYamlSVGGlyph() {
        super("/font/file/file-yaml.svg");
    }

    public FileYamlSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
