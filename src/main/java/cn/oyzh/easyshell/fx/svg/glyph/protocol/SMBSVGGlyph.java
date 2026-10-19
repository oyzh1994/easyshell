package cn.oyzh.easyshell.fx.svg.glyph.protocol;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * SMB协议SVG图标
 *
 * @author oyzh
 * @since 2025-06-29
 */
public class SMBSVGGlyph extends SVGGlyph {

    /**
     * 构造SMB协议SVG图标
     */
    public SMBSVGGlyph() {
        super("/font/protocol/smb.svg");
    }

    /**
     * 构造SMB协议SVG图标
     *
     * @param size 图标尺寸
     */
    public SMBSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
