package cn.oyzh.easyshell.fx.svg.glyph.protocol;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * Mosh 协议图标
 *
 * @author oyzh
 * @since 2025-03-05
 */
public class MoshSVGGlyph extends SVGGlyph {

    /**
     * 构造 Mosh 协议图标
     */
    public MoshSVGGlyph() {
        super("/font/protocol/mosh.svg");
    }

    /**
     * 构造指定尺寸的 Mosh 协议图标
     *
     * @param size 图标尺寸
     */
    public MoshSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
