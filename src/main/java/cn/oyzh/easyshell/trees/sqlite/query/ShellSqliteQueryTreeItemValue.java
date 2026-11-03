package cn.oyzh.easyshell.trees.sqlite.query;

import cn.oyzh.fx.gui.svg.glyph.QuerySVGGlyph;
import cn.oyzh.fx.gui.tree.view.RichTreeItemValue;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * SQLite查询节点值
 *
 * @author oyzh
 * @since 2026-11-02
 */
public class ShellSqliteQueryTreeItemValue extends RichTreeItemValue {

    /**
     * 构造查询节点值
     *
     * @param item 查询节点
     */
    public ShellSqliteQueryTreeItemValue(ShellSqliteQueryTreeItem item) {
        super(item);
        this.setRichMode(true);
    }

    @Override
    public ShellSqliteQueryTreeItem item() {
        return (ShellSqliteQueryTreeItem) super.item();
    }

    @Override
    public String name() {
        return this.item().queryName();
    }

    @Override
    public SVGGlyph graphic() {
        if (super.graphic() == null) {
            super.graphic(new QuerySVGGlyph());
        }
        return super.graphic();
    }
}
