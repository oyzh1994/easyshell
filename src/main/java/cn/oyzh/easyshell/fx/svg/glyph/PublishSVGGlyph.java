package cn.oyzh.easyshell.fx.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2024-10-16
 */
public class PublishSVGGlyph extends SVGGlyph {

    public PublishSVGGlyph() {
        super("/font/publish.svg");
    }

    public PublishSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
