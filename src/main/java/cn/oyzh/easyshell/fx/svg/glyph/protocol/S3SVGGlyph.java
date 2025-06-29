package cn.oyzh.easyshell.fx.svg.glyph.protocol;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class S3SVGGlyph extends SVGGlyph {

    public S3SVGGlyph() {
        super("/font/protocol/s3.svg");
    }

    public S3SVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
