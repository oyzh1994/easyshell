package cn.oyzh.easyshell.fx.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * 列表展开状态图标
 *
 * @author oyzh
 * @since 2025-09-01
 */
public class ExpandListSVGGlyph extends SVGGlyph {

    /**
     * 构造列表展开图标
     */
    public ExpandListSVGGlyph() {
        super("/font/arrow-down-double-line.svg");
    }

    /**
     * 构造指定尺寸的列表展开图标
     *
     * @param size 图标尺寸
     */
    public ExpandListSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
