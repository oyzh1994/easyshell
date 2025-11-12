package cn.oyzh.easyshell.trees.mysql.procedure;

import cn.oyzh.fx.gui.svg.glyph.database.ProcedureSVGGlyph;
import cn.oyzh.fx.gui.tree.view.RichTreeItemValue;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * db树表节点值
 *
 * @author oyzh
 * @since 2023/12/22
 */
public class ShellMysqlProcedureTreeItemValue extends RichTreeItemValue {

    public ShellMysqlProcedureTreeItemValue(ShellMysqlProcedureTreeItem item) {
        super(item);
    }

    @Override
    protected ShellMysqlProcedureTreeItem item() {
        return (ShellMysqlProcedureTreeItem) super.item();
    }

    @Override
    public SVGGlyph graphic() {
        if (this.graphic == null) {
            this.graphic = new ProcedureSVGGlyph("12");
        }
        return super.graphic();
    }

    @Override
    public String name() {
        return this.item().procedureName();
    }
}
