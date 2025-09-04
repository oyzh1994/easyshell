package cn.oyzh.easyshell.fx.svg.glyph.zk;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2024-10-16
 */
public class ZookeeperSVGGlyph extends SVGGlyph {

    public ZookeeperSVGGlyph() {
        super("/font/zk/zookeeper.svg");
    }

    public ZookeeperSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
