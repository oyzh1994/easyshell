package cn.oyzh.easyshell.fx.svg.glyph.zk;

import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * ZooKeeper 临时节点图标
 *
 * @author oyzh
 * @since 2025-09-04
 */
public class TempSVGGlyph extends SVGGlyph {

    /**
     * 构造 ZooKeeper 临时节点图标
     */
    public TempSVGGlyph() {
        super("/font/zk/temp.svg");
    }

    /**
     * 构造指定尺寸的 ZooKeeper 临时节点图标
     *
     * @param size 尺寸
     */
    public TempSVGGlyph(String size) {
        this();
        this.setSizeStr(size);
    }
}
