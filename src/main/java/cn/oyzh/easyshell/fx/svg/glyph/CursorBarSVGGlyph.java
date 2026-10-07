package cn.oyzh.easyshell.fx.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * 光标竖线样式图标
 *
 * @author oyzh
 * @since 2026-03-10
 */
public class CursorBarSVGGlyph extends SVGGlyph {

    /**
     * 构造光标竖线样式图标
     */
    public CursorBarSVGGlyph() {
        super("/font/cursor-bar.svg");
    }

    /**
     * 构造指定尺寸的光标竖线样式图标
     *
     * @param size 图标尺寸
     */
    public CursorBarSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
