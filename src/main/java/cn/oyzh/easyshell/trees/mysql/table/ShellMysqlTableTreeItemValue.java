package cn.oyzh.easyshell.trees.mysql.table;

import cn.oyzh.fx.gui.svg.glyph.database.TableSVGGlyph;
import cn.oyzh.fx.gui.tree.view.RichTreeItemValue;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * mysql表节点值
 *
 * @author oyzh
 * @since 2025-11-06
 */
public class ShellMysqlTableTreeItemValue extends RichTreeItemValue {

    /**
     * 构造表节点值
     *
     * @param item 表节点
     */
    public ShellMysqlTableTreeItemValue(ShellMysqlTableTreeItem item) {
        super(item);
        this.setRichMode(true);
    }

    @Override
    public ShellMysqlTableTreeItem item() {
        return (ShellMysqlTableTreeItem) super.item();
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
