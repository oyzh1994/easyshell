package cn.oyzh.easyshell.fx.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * 返回目录SVG图标
 *
 * @author oyzh
 * @since 2025-03-05
 */
public class ReturnFolderSVGGlyph extends SVGGlyph {

    /**
     * 构造返回目录SVG图标
     */
    public ReturnFolderSVGGlyph() {
        super("/font/return-folder.svg");
    }

    /**
     * 构造返回目录SVG图标
     *
     * @param size 图标尺寸
     */
    public ReturnFolderSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
