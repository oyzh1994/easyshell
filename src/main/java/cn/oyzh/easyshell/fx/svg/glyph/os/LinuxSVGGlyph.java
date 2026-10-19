package cn.oyzh.easyshell.fx.svg.glyph.os;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * Linux 操作系统图标
 *
 * @author oyzh
 * @since 2025-03-03
 */
public class LinuxSVGGlyph extends SVGGlyph {

    /**
     * 构造 Linux 操作系统图标
     */
    public LinuxSVGGlyph() {
        super("/font/linux.svg");
    }

    /**
     * 构造指定尺寸的 Linux 操作系统图标
     *
     * @param size 图标尺寸
     */
    public LinuxSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
