package cn.oyzh.easyshell.trees.postgresql.procedure;

import cn.oyzh.fx.gui.svg.glyph.database.ProcedureSVGGlyph;
import cn.oyzh.fx.gui.tree.view.RichTreeItemValue;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * PostgreSQL数据库树过程节点值
 *
 * @author oyzh
 * @since 2025-11-06
 */
public class ShellPostgresqlProcedureTreeItemValue extends RichTreeItemValue {

    /**
     * 构造PostgreSQL数据库树过程节点值
     *
     * @param item 过程节点
     */
    public ShellPostgresqlProcedureTreeItemValue(ShellPostgresqlProcedureTreeItem item) {
        super(item);
        this.setRichMode(true);
    }

    @Override
    public ShellPostgresqlProcedureTreeItem item() {
        return (ShellPostgresqlProcedureTreeItem) super.item();
    }

    @Override
    public SVGGlyph graphic() {
        if (super.graphic() == null) {
            super.graphic(new ProcedureSVGGlyph());
        }
        return super.graphic();
    }

    @Override
    public String name() {
        return this.item().procedureName();
    }
}
