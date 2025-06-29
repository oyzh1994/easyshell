package cn.oyzh.easyshell.fx.svg.glyph.other;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class MinioSVGGlyph extends SVGGlyph {

    public MinioSVGGlyph() {
        super("/font/other/minio.svg");
    }

    public MinioSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
