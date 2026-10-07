package cn.oyzh.easyshell.fx.svg.glyph.zk;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * ZooKeeper 节点图标
 *
 * @author oyzh
 * @since 2025-01-23
 */
public class NodeSVGGlyph extends SVGGlyph {

    /**
     * 构造 ZooKeeper 节点图标
     */
    public NodeSVGGlyph() {
        super("/font/zk/file-text.svg");
    }

    /**
     * 构造指定尺寸的 ZooKeeper 节点图标
     *
     * @param size 尺寸
     */
    public NodeSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
