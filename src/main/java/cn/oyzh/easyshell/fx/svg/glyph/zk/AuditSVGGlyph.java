package cn.oyzh.easyshell.fx.svg.glyph.zk;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * ZooKeeper 审计节点图标
 *
 * @author oyzh
 * @since 2025-01-23
 */
public class AuditSVGGlyph extends SVGGlyph {

    /**
     * 构造 ZooKeeper 审计节点图标
     */
    public AuditSVGGlyph() {
        super("/font/zk/audit.svg");
    }

    /**
     * 构造指定尺寸的 ZooKeeper 审计节点图标
     *
     * @param size 图标尺寸
     */
    public AuditSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
