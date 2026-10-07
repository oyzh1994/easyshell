package cn.oyzh.easyshell.fx.svg.glyph.other;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * 腾讯云SVG图标
 *
 * @author oyzh
 * @since 2025-03-05
 */
public class TencentCloudSVGGlyph extends SVGGlyph {

    /**
     * 构造腾讯云SVG图标
     */
    public TencentCloudSVGGlyph() {
        super("/font/other/tencent_cloud.svg");
    }

    /**
     * 构造腾讯云SVG图标
     *
     * @param size 图标尺寸
     */
    public TencentCloudSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
