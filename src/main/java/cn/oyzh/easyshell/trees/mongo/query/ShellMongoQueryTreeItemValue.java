package cn.oyzh.easyshell.trees.mongo.query;

import cn.oyzh.fx.gui.svg.glyph.QuerySVGGlyph;
import cn.oyzh.fx.gui.tree.view.RichTreeItemValue;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * mongodb树查询节点值
 *
 * @author oyzh
 * @since 2025-11-06
 */
public class ShellMongoQueryTreeItemValue extends RichTreeItemValue {

    /**
     * 构造查询节点值
     *
     * @param item 查询节点
     */
    public ShellMongoQueryTreeItemValue(ShellMongoQueryTreeItem item) {
        super(item);
        super.setRichMode(true);
    }

    @Override
    public ShellMongoQueryTreeItem item() {
        return (ShellMongoQueryTreeItem) super.item();
    }

    @Override
    public SVGGlyph graphic() {
        if (super.graphic() == null) {
            super.graphic(new QuerySVGGlyph());
        }
        return super.graphic();
    }

    @Override
    public String name() {
        return this.item().queryName();
    }
}
