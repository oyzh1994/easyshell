package cn.oyzh.easyshell.fx.svg.glyph.file.a;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class FileAsciidoctorSVGGlyph extends SVGGlyph {

    public FileAsciidoctorSVGGlyph() {
        super("/font/file/a/file-asciidoctor.svg");
    }

    public FileAsciidoctorSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
