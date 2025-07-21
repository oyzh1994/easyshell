package cn.oyzh.easyshell.fx.svg.glyph.other;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class AlibabaCloudSVGGlyph extends SVGGlyph {

    public AlibabaCloudSVGGlyph() {
        super("/font/other/alibaba_cloud.svg");
    }

    public AlibabaCloudSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
