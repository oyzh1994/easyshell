package cn.oyzh.easyshell.fx.svg.glyph.os;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * Apple（macOS）操作系统图标
 *
 * @author oyzh
 * @since 2025-03-27
 */
public class AppleSVGGlyph extends SVGGlyph {

    /**
     * 构造 Apple（macOS）操作系统图标
     */
    public AppleSVGGlyph() {
        super("/font/os/apple.svg");
    }

    /**
     * 构造指定尺寸的 Apple（macOS）操作系统图标
     *
     * @param size 图标尺寸
     */
    public AppleSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
