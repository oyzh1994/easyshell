package cn.oyzh.easyshell.trees.postgresql.view;

import cn.oyzh.fx.gui.svg.glyph.database.ViewSVGGlyph;
import cn.oyzh.fx.gui.tree.view.RichTreeItemValue;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * PostgreSQL数据库树视图节点值
 *
 * @author oyzh
 * @since 2025-11-06
 */
public class ShellPostgresqlViewTreeItemValue extends RichTreeItemValue {

    /**
     * 构造PostgreSQL数据库树视图节点值
     *
     * @param item 视图节点
     */
    public ShellPostgresqlViewTreeItemValue(ShellPostgresqlViewTreeItem item) {
        super(item);
        this.setRichMode(true);
    }

    @Override
    public ShellPostgresqlViewTreeItem item() {
        return (ShellPostgresqlViewTreeItem) super.item();
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
