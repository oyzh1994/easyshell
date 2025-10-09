package cn.oyzh.easyshell.fx.svg.glyph.protocol;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class WebdavSVGGlyph extends SVGGlyph {

    public WebdavSVGGlyph() {
        super("/font/protocol/webdav.svg");
    }

    public WebdavSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
