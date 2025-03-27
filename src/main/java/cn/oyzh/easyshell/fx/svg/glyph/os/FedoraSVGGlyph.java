package cn.oyzh.easyshell.fx.svg.glyph.os;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-03-05
 */
public class FedoraSVGGlyph extends SVGGlyph {

    public FedoraSVGGlyph() {
        super("/font/os/fedora.svg");
    }

    public FedoraSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
