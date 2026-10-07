package cn.oyzh.easyshell.trees.dameng.procedure;

import cn.oyzh.fx.gui.svg.glyph.database.ProcedureSVGGlyph;
import cn.oyzh.fx.gui.tree.view.RichTreeItemValue;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * 达梦数据库树过程节点值
 *
 * @author oyzh
 * @since 2025-11-06
 */
public class ShellDamengProcedureTreeItemValue extends RichTreeItemValue {

    /**
     * 构造达梦数据库树过程节点值
     *
     * @param item 过程节点
     */
    public ShellDamengProcedureTreeItemValue(ShellDamengProcedureTreeItem item) {
        super(item);
    }

    @Override
    public ShellDamengProcedureTreeItem item() {
        return (ShellDamengProcedureTreeItem) super.item();
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
