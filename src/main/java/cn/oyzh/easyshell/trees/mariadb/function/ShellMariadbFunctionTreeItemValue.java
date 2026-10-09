package cn.oyzh.easyshell.trees.mariadb.function;

import cn.oyzh.fx.gui.svg.glyph.database.FunctionSVGGlyph;
import cn.oyzh.fx.gui.tree.view.RichTreeItemValue;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * MariaDB函数节点值
 *
 * @author oyzh
 * @since 2026-10-09
 */
public class ShellMariadbFunctionTreeItemValue extends RichTreeItemValue {

    /**
     * 构造函数节点值
     *
     * @param item 函数节点
     */
    public ShellMariadbFunctionTreeItemValue(ShellMariadbFunctionTreeItem item) {
        super(item);
        this.setRichMode(true);
    }

    @Override
    public ShellMariadbFunctionTreeItem item() {
        return (ShellMariadbFunctionTreeItem) super.item();
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
