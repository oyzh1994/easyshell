package cn.oyzh.easyshell.trees.mariadb.procedure;

import cn.oyzh.fx.gui.svg.glyph.database.ProcedureSVGGlyph;
import cn.oyzh.fx.gui.tree.view.RichTreeItemValue;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * MariaDB过程节点值
 *
 * @author oyzh
 * @since 2026-10-09
 */
public class ShellMariadbProcedureTreeItemValue extends RichTreeItemValue {

    /**
     * 构造过程节点值
     *
     * @param item 过程节点
     */
    public ShellMariadbProcedureTreeItemValue(ShellMariadbProcedureTreeItem item) {
        super(item);
        this.setRichMode(true);
    }

    @Override
    public ShellMariadbProcedureTreeItem item() {
        return (ShellMariadbProcedureTreeItem) super.item();
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
