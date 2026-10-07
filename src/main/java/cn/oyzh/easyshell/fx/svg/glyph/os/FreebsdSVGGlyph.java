package cn.oyzh.easyshell.fx.svg.glyph.os;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * FreeBSD 操作系统图标
 *
 * @author oyzh
 * @since 2025-03-05
 */
public class FreebsdSVGGlyph extends SVGGlyph {

    /**
     * 构造 FreeBSD 操作系统图标
     */
    public FreebsdSVGGlyph() {
        super("/font/os/freebsd.svg");
    }

    /**
     * 构造指定尺寸的 FreeBSD 操作系统图标
     *
     * @param size 图标尺寸
     */
    public FreebsdSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
