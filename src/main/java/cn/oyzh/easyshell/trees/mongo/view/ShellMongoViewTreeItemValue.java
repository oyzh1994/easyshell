package cn.oyzh.easyshell.trees.mongo.view;

import cn.oyzh.fx.gui.svg.glyph.database.ViewSVGGlyph;
import cn.oyzh.fx.gui.tree.view.RichTreeItemValue;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * MongoDB 视图节点值。
 *
 * @author oyzh
 * @since 2026-11-25
 */
public class ShellMongoViewTreeItemValue extends RichTreeItemValue {

    public ShellMongoViewTreeItemValue(ShellMongoViewTreeItem item) {
        super(item);
        super.setRichMode(true);
    }

    @Override
    public ShellMongoViewTreeItem item() {
        return (ShellMongoViewTreeItem) super.item();
    }

    @Override
    public SVGGlyph graphic() {
        if (super.graphic() == null) {
            super.graphic(new ViewSVGGlyph());
        }
        return super.graphic();
    }

    @Override
    public String name() {
        return this.item().viewName();
    }
}
