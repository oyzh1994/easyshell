package cn.oyzh.easyshell.trees.sqlite.view;

import cn.oyzh.fx.gui.svg.glyph.database.ViewSVGGlyph;
import cn.oyzh.fx.gui.tree.view.RichTreeItemValue;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * SQLite视图节点值
 *
 * @author oyzh
 * @since 2026-11-02
 */
public class ShellSqliteViewTreeItemValue extends RichTreeItemValue {

    /**
     * 构造视图节点值
     *
     * @param item 视图节点
     */
    public ShellSqliteViewTreeItemValue(ShellSqliteViewTreeItem item) {
        super(item);
        this.setRichMode(true);
    }

    @Override
    public ShellSqliteViewTreeItem item() {
        return (ShellSqliteViewTreeItem) super.item();
    }

    @Override
    public String name() {
        return this.item().viewName();
    }

    @Override
    public SVGGlyph graphic() {
        if (super.graphic() == null) {
            super.graphic(new ViewSVGGlyph());
        }
        return super.graphic();
    }
}
