package cn.oyzh.easyshell.trees.mysql.function;

import cn.oyzh.fx.gui.svg.glyph.database.FunctionSVGGlyph;
import cn.oyzh.fx.gui.tree.view.RichTreeItemValue;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * mysql函数节点值
 *
 * @author oyzh
 * @since 2025-11-06
 */
public class ShellMysqlFunctionTreeItemValue extends RichTreeItemValue {

    /**
     * 构造函数节点值
     *
     * @param item 函数节点
     */
    public ShellMysqlFunctionTreeItemValue(ShellMysqlFunctionTreeItem item) {
        super(item);
        this.setRichMode(true);
    }

    @Override
    public ShellMysqlFunctionTreeItem item() {
        return (ShellMysqlFunctionTreeItem) super.item();
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
