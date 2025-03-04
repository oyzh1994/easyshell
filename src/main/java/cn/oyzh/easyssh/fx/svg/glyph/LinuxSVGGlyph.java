package cn.oyzh.easyssh.fx.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2024-10-16
 */
public class LinuxSVGGlyph extends SVGGlyph {

    public LinuxSVGGlyph() {
        super("/font/linux.svg");
    }

    public LinuxSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
