package cn.oyzh.easyshell.fx.svg.glyph.file.t;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class FileTwigSVGGlyph extends SVGGlyph {

    public FileTwigSVGGlyph() {
        super("/font/file/t/file-twig.svg");
    }

    public FileTwigSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
