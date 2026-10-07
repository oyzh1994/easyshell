package cn.oyzh.easyshell.fx.svg.glyph.protocol;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * Telnet 协议图标
 *
 * @author oyzh
 * @since 2025-03-05
 */
public class TelnetSVGGlyph extends SVGGlyph {

    /**
     * 构造 Telnet 协议图标
     */
    public TelnetSVGGlyph() {
        super("/font/protocol/telnet.svg");
    }

    /**
     * 构造指定尺寸的 Telnet 协议图标
     *
     * @param size 尺寸
     */
    public TelnetSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
