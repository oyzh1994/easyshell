package cn.oyzh.easyshell.fx.svg.glyph.other;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * 华为云图标
 *
 * @author oyzh
 * @since 2025-03-05
 */
public class HuaweiCloudSVGGlyph extends SVGGlyph {

    /**
     * 构造华为云图标
     */
    public HuaweiCloudSVGGlyph() {
        super("/font/other/huawei_cloud.svg");
    }

    /**
     * 构造指定尺寸的华为云图标
     *
     * @param size 图标尺寸
     */
    public HuaweiCloudSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
