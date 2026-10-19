package cn.oyzh.easyshell.trees.mariadb.table;

import cn.oyzh.fx.gui.svg.glyph.database.TableSVGGlyph;
import cn.oyzh.fx.gui.tree.view.RichTreeItemValue;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * MariaDB表节点值
 *
 * @author oyzh
 * @since 2026-10-09
 */
public class ShellMariadbTableTreeItemValue extends RichTreeItemValue {

    /**
     * 构造表节点值
     *
     * @param item 表节点
     */
    public ShellMariadbTableTreeItemValue(ShellMariadbTableTreeItem item) {
        super(item);
        this.setRichMode(true);
    }

    @Override
    public ShellMariadbTableTreeItem item() {
        return (ShellMariadbTableTreeItem) super.item();
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
