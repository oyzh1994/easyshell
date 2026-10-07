package cn.oyzh.easyshell.fx.svg.glyph.protocol;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * SSH协议SVG图标
 *
 * @author oyzh
 * @since 2024-10-16
 */
public class SSHSVGGlyph extends SVGGlyph {

    /**
     * 构造SSH协议SVG图标
     */
    public SSHSVGGlyph() {
        super("/font/ssh.svg");
    }

    /**
     * 构造SSH协议SVG图标
     *
     * @param size 图标尺寸
     */
    public SSHSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
