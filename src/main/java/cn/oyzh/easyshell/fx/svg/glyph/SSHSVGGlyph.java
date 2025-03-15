package cn.oyzh.easyshell.fx.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2024-10-16
 */
public class SSHSVGGlyph extends SVGGlyph {

    public SSHSVGGlyph() {
        super("/font/ssh.svg");
    }

    public SSHSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
