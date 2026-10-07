package cn.oyzh.easyshell.fx.svg.glyph.protocol;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * FTP 协议图标
 *
 * @author oyzh
 * @since 2025-06-29
 */
public class FTPSVGGlyph extends SVGGlyph {

    /**
     * 构造 FTP 协议图标
     */
    public FTPSVGGlyph() {
        super("/font/protocol/ftp.svg");
    }

    /**
     * 构造指定尺寸的 FTP 协议图标
     *
     * @param size 图标尺寸
     */
    public FTPSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
