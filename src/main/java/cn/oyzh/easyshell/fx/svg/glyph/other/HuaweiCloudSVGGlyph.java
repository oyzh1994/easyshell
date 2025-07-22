package cn.oyzh.easyshell.fx.svg.glyph.other;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class HuaweiCloudSVGGlyph extends SVGGlyph {

    public HuaweiCloudSVGGlyph() {
        super("/font/other/huawei_cloud.svg");
    }

    public HuaweiCloudSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
