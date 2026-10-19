package cn.oyzh.easyshell.fx.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * 光标方块样式图标
 *
 * @author oyzh
 * @since 2026-03-10
 */
public class CursorBlockSVGGlyph extends SVGGlyph {

    /**
     * 构造光标方块样式图标
     */
    public CursorBlockSVGGlyph() {
        super("/font/cursor-block.svg");
    }

    /**
     * 构造指定尺寸的光标方块样式图标
     *
     * @param size 图标尺寸
     */
    public CursorBlockSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
