package cn.oyzh.easyshell.trees.sqlserver.view;

import cn.oyzh.fx.gui.svg.glyph.database.ViewSVGGlyph;
import cn.oyzh.fx.gui.tree.view.RichTreeItemValue;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * SqlServer数据库树视图节点值
 *
 * @author oyzh
 * @since 2025-11-06
 */
public class ShellSqlServerViewTreeItemValue extends RichTreeItemValue {

    /**
     * 构造SqlServer数据库树视图节点值
     *
     * @param item 视图节点
     */
    public ShellSqlServerViewTreeItemValue(ShellSqlServerViewTreeItem item) {
        super(item);
        this.setRichMode(true);
    }

    @Override
    public ShellSqlServerViewTreeItem item() {
        return (ShellSqlServerViewTreeItem) super.item();
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
