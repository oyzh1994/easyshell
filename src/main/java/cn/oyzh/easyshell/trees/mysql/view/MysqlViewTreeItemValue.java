package cn.oyzh.easyshell.trees.mysql.view;

import cn.oyzh.easyshell.trees.mysql.view.MysqlViewTreeItem;
import cn.oyzh.fx.gui.svg.glyph.database.ViewSVGGlyph;
import cn.oyzh.fx.gui.tree.view.RichTreeItemValue;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * db树表节点值
 *
 * @author oyzh
 * @since 2023/12/22
 */
public class MysqlViewTreeItemValue extends RichTreeItemValue {

    public MysqlViewTreeItemValue(MysqlViewTreeItem item) {
        super(item);
    }

    @Override
    protected MysqlViewTreeItem item() {
        return (MysqlViewTreeItem) super.item();
    }

    @Override
    public SVGGlyph graphic() {
        if (this.graphic == null) {
            this.graphic  = new ViewSVGGlyph("12");
        }
        return super.graphic();
    }

    @Override
    public String name() {
        return this.item().viewName();
    }
}
