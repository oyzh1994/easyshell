package cn.oyzh.easyshell.fx.svg.glyph.protocol;

import cn.oyzh.fx.plus.controls.svg.ScalingSVGGlyph;

/**
 * S3协议SVG图标
 *
 * @author oyzh
 * @since 2025-06-29
 */
public class S3SVGGlyph extends ScalingSVGGlyph {

    /**
     * 构造S3协议SVG图标
     */
    public S3SVGGlyph() {
        super("/font/protocol/s3.svg");
    }

    /**
     * 构造S3协议SVG图标
     *
     * @param size 图标尺寸
     */
    public S3SVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }

    @Override
    public double widthScaling() {
        return 0.85;
    }
}
