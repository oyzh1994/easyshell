package cn.oyzh.easyshell.fx.svg.glyph;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * 列表折叠状态图标
 *
 * @author oyzh
 * @since 2025-09-01
 */
public class CollapseListSVGGlyph extends SVGGlyph {

    /**
     * 构造列表折叠图标
     */
    public CollapseListSVGGlyph() {
        super("/font/arrow-up-double-line.svg");
    }

    /**
     * 构造指定尺寸的列表折叠图标
     *
     * @param size 图标尺寸
     */
    public CollapseListSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
