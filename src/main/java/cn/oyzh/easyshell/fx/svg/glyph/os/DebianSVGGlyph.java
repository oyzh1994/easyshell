package cn.oyzh.easyshell.fx.svg.glyph.os;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * Debian 操作系统图标
 *
 * @author oyzh
 * @since 2025-03-27
 */
public class DebianSVGGlyph extends SVGGlyph {

    /**
     * 构造 Debian 操作系统图标
     */
    public DebianSVGGlyph() {
        super("/font/os/debian.svg");
    }

    /**
     * 构造指定尺寸的 Debian 操作系统图标
     *
     * @param size 图标尺寸
     */
    public DebianSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
