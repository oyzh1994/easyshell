package cn.oyzh.easyshell.trees.sqlite.table;

import cn.oyzh.fx.gui.svg.glyph.database.TableSVGGlyph;
import cn.oyzh.fx.gui.tree.view.RichTreeItemValue;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * SQLite表节点值
 *
 * @author oyzh
 * @since 2026-11-02
 */
public class ShellSqliteTableTreeItemValue extends RichTreeItemValue {

    /**
     * 构造表节点值
     *
     * @param item 表节点
     */
    public ShellSqliteTableTreeItemValue(ShellSqliteTableTreeItem item) {
        super(item);
        this.setRichMode(true);
    }

    @Override
    public ShellSqliteTableTreeItem item() {
        return (ShellSqliteTableTreeItem) super.item();
    }

    @Override
    public SVGGlyph graphic() {
        if (super.graphic() == null) {
            super.graphic(new TableSVGGlyph());
        }
        return super.graphic();
    }

    @Override
    public String name() {
        return this.item().tableName();
    }
}
