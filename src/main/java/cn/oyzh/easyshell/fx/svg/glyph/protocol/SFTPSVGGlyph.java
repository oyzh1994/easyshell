package cn.oyzh.easyshell.fx.svg.glyph.protocol;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * SFTP协议SVG图标
 *
 * @author oyzh
 * @since 2025-03-05
 */
public class SFTPSVGGlyph extends SVGGlyph {

    /**
     * 构造SFTP协议SVG图标
     */
    public SFTPSVGGlyph() {
        super("/font/protocol/sftp.svg");
    }

    /**
     * 构造SFTP协议SVG图标
     *
     * @param size 图标尺寸
     */
    public SFTPSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
