package cn.oyzh.easyshell.fx.svg.glyph.protocol;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.fx.plus.controls.svg.ScalingSVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class S3SVGGlyph extends ScalingSVGGlyph {

    public S3SVGGlyph() {
        super("/font/protocol/s3.svg");
    }

    public S3SVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }

    @Override
    public double widthScaling() {
        return 0.85;
    }
}
