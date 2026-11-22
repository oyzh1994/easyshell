package cn.oyzh.easyshell.trees.sqlserver.query;

import cn.oyzh.fx.gui.svg.glyph.QuerySVGGlyph;
import cn.oyzh.fx.gui.tree.view.RichTreeItemValue;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * SqlServer数据库树查询节点值
 *
 * @author oyzh
 * @since 2025-11-06
 */
public class ShellSqlServerQueryTreeItemValue extends RichTreeItemValue {

    /**
     * 构造SqlServer数据库树查询节点值
     *
     * @param item 查询节点
     */
    public ShellSqlServerQueryTreeItemValue(ShellSqlServerQueryTreeItem item) {
        super(item);
        this.setRichMode(true);
    }

    @Override
    public ShellSqlServerQueryTreeItem item() {
        return (ShellSqlServerQueryTreeItem) super.item();
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
