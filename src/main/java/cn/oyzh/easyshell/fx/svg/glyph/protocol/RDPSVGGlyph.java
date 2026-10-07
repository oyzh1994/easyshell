package cn.oyzh.easyshell.fx.svg.glyph.protocol;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * RDP协议SVG图标
 *
 * @author oyzh
 * @since 2025-03-05
 */
public class RDPSVGGlyph extends SVGGlyph {

    /**
     * 构造RDP协议SVG图标
     */
    public RDPSVGGlyph() {
        super("/font/protocol/rdp.svg");
    }

    /**
     * 构造RDP协议SVG图标
     *
     * @param size 图标尺寸
     */
    public RDPSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
