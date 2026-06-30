package cn.oyzh.easyshell.trees.mongo.query;

import cn.oyzh.fx.gui.svg.glyph.QuerySVGGlyph;
import cn.oyzh.fx.gui.tree.view.RichTreeItemValue;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * db树表节点值
 *
 * @author oyzh
 * @since 2023/12/22
 */
public class MongoQueryTreeItemValue extends RichTreeItemValue {

    public MongoQueryTreeItemValue(MongoQueryTreeItem item) {
        super(item);
        super.setRichMode(true);
    }

    @Override
    public MongoQueryTreeItem item() {
        return (MongoQueryTreeItem) super.item();
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
