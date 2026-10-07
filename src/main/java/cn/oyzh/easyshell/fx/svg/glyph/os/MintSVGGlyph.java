package cn.oyzh.easyshell.fx.svg.glyph.os;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * Linux Mint 操作系统图标
 *
 * @author oyzh
 * @since 2025-03-05
 */
public class MintSVGGlyph extends SVGGlyph {

    /**
     * 构造 Linux Mint 操作系统图标
     */
    public MintSVGGlyph() {
        super("/font/os/mint.svg");
    }

    /**
     * 构造指定尺寸的 Linux Mint 操作系统图标
     *
     * @param size 图标尺寸
     */
    public MintSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
