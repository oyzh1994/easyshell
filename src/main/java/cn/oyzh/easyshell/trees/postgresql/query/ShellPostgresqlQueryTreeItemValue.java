package cn.oyzh.easyshell.trees.postgresql.query;

import cn.oyzh.fx.gui.svg.glyph.QuerySVGGlyph;
import cn.oyzh.fx.gui.tree.view.RichTreeItemValue;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * PostgreSQL数据库树查询节点值
 *
 * @author oyzh
 * @since 2025-11-06
 */
public class ShellPostgresqlQueryTreeItemValue extends RichTreeItemValue {

    /**
     * 构造PostgreSQL数据库树查询节点值
     *
     * @param item 查询节点
     */
    public ShellPostgresqlQueryTreeItemValue(ShellPostgresqlQueryTreeItem item) {
        super(item);
        this.setRichMode(true);
    }

    @Override
    public ShellPostgresqlQueryTreeItem item() {
        return (ShellPostgresqlQueryTreeItem) super.item();
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
