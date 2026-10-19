package cn.oyzh.easyshell.fx.svg.glyph.protocol;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * RLogin协议SVG图标
 *
 * @author oyzh
 * @since 2025-06-29
 */
public class RLoginSVGGlyph extends SVGGlyph {

    /**
     * 构造RLogin协议SVG图标
     */
    public RLoginSVGGlyph() {
        super("/font/protocol/rlogin.svg");
    }

    /**
     * 构造RLogin协议SVG图标
     *
     * @param size 图标尺寸
     */
    public RLoginSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
