package cn.oyzh.easyshell.fx.svg.glyph.zk;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-01-23
 */
public class NodeSVGGlyph extends SVGGlyph {

    public NodeSVGGlyph() {
        super("/font/zk/file-text.svg");
    }

    public NodeSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
