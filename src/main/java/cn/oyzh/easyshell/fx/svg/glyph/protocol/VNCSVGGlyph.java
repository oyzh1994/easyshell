package cn.oyzh.easyshell.fx.svg.glyph.protocol;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * VNC 协议图标
 *
 * @author oyzh
 * @since 2025-06-29
 */
public class VNCSVGGlyph extends SVGGlyph {

    /**
     * 构造 VNC 协议图标
     */
    public VNCSVGGlyph() {
        super("/font/protocol/vnc.svg");
    }

    /**
     * 构造指定尺寸的 VNC 协议图标
     *
     * @param size 尺寸
     */
    public VNCSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
