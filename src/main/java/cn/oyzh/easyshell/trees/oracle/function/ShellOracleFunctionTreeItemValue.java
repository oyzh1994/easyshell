package cn.oyzh.easyshell.trees.oracle.function;

import cn.oyzh.fx.gui.svg.glyph.database.FunctionSVGGlyph;
import cn.oyzh.fx.gui.tree.view.RichTreeItemValue;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * Oracle数据库树函数节点值
 *
 * @author oyzh
 * @since 2025-11-06
 */
public class ShellOracleFunctionTreeItemValue extends RichTreeItemValue {

    /**
     * 构造Oracle数据库树函数节点值
     *
     * @param item 函数节点
     */
    public ShellOracleFunctionTreeItemValue(ShellOracleFunctionTreeItem item) {
        super(item);
        this.setRichMode(true);
    }

    @Override
    public ShellOracleFunctionTreeItem item() {
        return (ShellOracleFunctionTreeItem) super.item();
    }

    @Override
    public SVGGlyph graphic() {
        if (super.graphic() == null) {
            super.graphic(new FunctionSVGGlyph());
        }
        return super.graphic();
    }

    @Override
    public String name() {
        return this.item().functionName();
    }
}
