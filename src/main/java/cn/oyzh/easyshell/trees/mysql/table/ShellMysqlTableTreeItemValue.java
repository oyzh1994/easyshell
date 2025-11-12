package cn.oyzh.easyshell.trees.mysql.table;

import cn.oyzh.fx.gui.svg.glyph.database.TableSVGGlyph;
import cn.oyzh.fx.gui.tree.view.RichTreeItemValue;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * db树表节点值
 *
 * @author oyzh
 * @since 2023/12/22
 */
public class ShellMysqlTableTreeItemValue extends RichTreeItemValue {

    public ShellMysqlTableTreeItemValue(ShellMysqlTableTreeItem item) {
        super(item);
    }

    @Override
    protected ShellMysqlTableTreeItem item() {
        return (ShellMysqlTableTreeItem) super.item();
    }

    @Override
    public SVGGlyph graphic() {
        if (this.graphic == null) {
            this.graphic = new TableSVGGlyph("12");
        }
        return super.graphic();
    }

    @Override
    public String name() {
        return this.item().tableName();
    }
}
