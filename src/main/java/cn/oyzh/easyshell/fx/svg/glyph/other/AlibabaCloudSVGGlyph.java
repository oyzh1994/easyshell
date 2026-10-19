package cn.oyzh.easyshell.fx.svg.glyph.other;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * 阿里云图标
 *
 * @author oyzh
 * @since 2025-06-29
 */
public class AlibabaCloudSVGGlyph extends SVGGlyph {

    /**
     * 构造阿里云图标
     */
    public AlibabaCloudSVGGlyph() {
        super("/font/other/alibaba_cloud.svg");
    }

    /**
     * 构造指定尺寸的阿里云图标
     *
     * @param size 图标尺寸
     */
    public AlibabaCloudSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
