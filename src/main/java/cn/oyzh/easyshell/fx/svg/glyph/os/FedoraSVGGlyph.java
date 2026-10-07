package cn.oyzh.easyshell.fx.svg.glyph.os;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * Fedora 操作系统图标
 *
 * @author oyzh
 * @since 2025-03-27
 */
public class FedoraSVGGlyph extends SVGGlyph {

    /**
     * 构造 Fedora 操作系统图标
     */
    public FedoraSVGGlyph() {
        super("/font/os/fedora.svg");
    }

    /**
     * 构造指定尺寸的 Fedora 操作系统图标
     *
     * @param size 图标尺寸
     */
    public FedoraSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
