package cn.oyzh.easyshell.trees.mysql.view;

import cn.oyzh.fx.gui.svg.glyph.database.ViewSVGGlyph;
import cn.oyzh.fx.gui.tree.view.RichTreeItemValue;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * db树表节点值
 *
 * @author oyzh
 * @since 2023/12/22
 */
public class ShellMysqlViewTreeItemValue extends RichTreeItemValue {

    public ShellMysqlViewTreeItemValue(ShellMysqlViewTreeItem item) {
        super(item);
    }

    @Override
    protected ShellMysqlViewTreeItem item() {
        return (ShellMysqlViewTreeItem) super.item();
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
