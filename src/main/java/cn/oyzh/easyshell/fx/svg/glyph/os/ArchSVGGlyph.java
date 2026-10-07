package cn.oyzh.easyshell.fx.svg.glyph.os;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * Arch Linux 操作系统图标
 *
 * @author oyzh
 * @since 2025-03-27
 */
public class ArchSVGGlyph extends SVGGlyph {

    /**
     * 构造 Arch Linux 操作系统图标
     */
    public ArchSVGGlyph() {
        super("/font/os/arch.svg");
    }

    /**
     * 构造指定尺寸的 Arch Linux 操作系统图标
     *
     * @param size 图标尺寸
     */
    public ArchSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
