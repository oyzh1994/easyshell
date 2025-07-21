package cn.oyzh.easyshell.fx.svg.glyph.other;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class TencentCloudSVGGlyph extends SVGGlyph {

    public TencentCloudSVGGlyph() {
        super("/font/other/tencent_cloud.svg");
    }

    public TencentCloudSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
