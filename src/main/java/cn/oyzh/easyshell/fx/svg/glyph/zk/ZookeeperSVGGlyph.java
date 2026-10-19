package cn.oyzh.easyshell.fx.svg.glyph.zk;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * ZooKeeper 图标
 *
 * @author oyzh
 * @since 2025-09-04
 */
public class ZookeeperSVGGlyph extends SVGGlyph {

    /**
     * 构造 ZooKeeper 图标
     */
    public ZookeeperSVGGlyph() {
        super("/font/zk/zookeeper.svg");
    }

    /**
     * 构造指定尺寸的 ZooKeeper 图标
     *
     * @param size 尺寸
     */
    public ZookeeperSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
