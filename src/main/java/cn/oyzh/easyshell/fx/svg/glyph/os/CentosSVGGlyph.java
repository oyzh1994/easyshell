package cn.oyzh.easyshell.fx.svg.glyph.os;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * CentOS 操作系统图标
 *
 * @author oyzh
 * @since 2025-03-27
 */
public class CentosSVGGlyph extends SVGGlyph {

    /**
     * 构造 CentOS 操作系统图标
     */
    public CentosSVGGlyph() {
        super("/font/os/centos.svg");
    }

    /**
     * 构造指定尺寸的 CentOS 操作系统图标
     *
     * @param size 图标尺寸
     */
    public CentosSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
