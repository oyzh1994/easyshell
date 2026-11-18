package cn.oyzh.easyshell.trees.oracle.procedure;

import cn.oyzh.fx.gui.svg.glyph.database.ProcedureSVGGlyph;
import cn.oyzh.fx.gui.tree.view.RichTreeItemValue;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * Oracle数据库树过程节点值
 *
 * @author oyzh
 * @since 2025-11-06
 */
public class ShellOracleProcedureTreeItemValue extends RichTreeItemValue {

    /**
     * 构造Oracle数据库树过程节点值
     *
     * @param item 过程节点
     */
    public ShellOracleProcedureTreeItemValue(ShellOracleProcedureTreeItem item) {
        super(item);
        this.setRichMode(true);
    }

    @Override
    public ShellOracleProcedureTreeItem item() {
        return (ShellOracleProcedureTreeItem) super.item();
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
