package cn.oyzh.easyshell.fx.svg.glyph.os;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * Deepin 操作系统图标
 *
 * @author oyzh
 * @since 2025-03-05
 */
public class DeepinSVGGlyph extends SVGGlyph {

    /**
     * 构造 Deepin 操作系统图标
     */
    public DeepinSVGGlyph() {
        super("/font/os/deepin.svg");
    }

    /**
     * 构造指定尺寸的 Deepin 操作系统图标
     *
     * @param size 图标尺寸
     */
    public DeepinSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
