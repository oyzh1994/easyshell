package cn.oyzh.easyshell.fx.svg.glyph.zk;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2025-01-23
 */
public class AuditSVGGlyph extends SVGGlyph {

    public AuditSVGGlyph() {
        super("/font/zk/audit.svg");
    }

    public AuditSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
