package cn.oyzh.easyshell.fx.svg.glyph.protocol;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * WebDAV 协议图标
 *
 * @author oyzh
 * @since 2025-03-05
 */
public class WebdavSVGGlyph extends SVGGlyph {

    /**
     * 构造 WebDAV 协议图标
     */
    public WebdavSVGGlyph() {
        super("/font/protocol/webdav.svg");
    }

    /**
     * 构造指定尺寸的 WebDAV 协议图标
     *
     * @param size 尺寸
     */
    public WebdavSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
