package cn.oyzh.easyshell.fx.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * 光标下划线样式图标
 *
 * @author oyzh
 * @since 2026-03-10
 */
public class CursorUnderlineSVGGlyph extends SVGGlyph {

    /**
     * 构造光标下划线样式图标
     */
    public CursorUnderlineSVGGlyph() {
        super("/font/cursor-underline.svg");
    }

    /**
     * 构造指定尺寸的光标下划线样式图标
     *
     * @param size 图标尺寸
     */
    public CursorUnderlineSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
