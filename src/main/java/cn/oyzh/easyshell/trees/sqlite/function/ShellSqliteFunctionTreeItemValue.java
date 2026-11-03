package cn.oyzh.easyshell.trees.sqlite.function;

import cn.oyzh.fx.gui.svg.glyph.database.FunctionSVGGlyph;
import cn.oyzh.fx.gui.tree.view.RichTreeItemValue;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * SQLite函数节点值
 *
 * @author oyzh
 * @since 2026-11-02
 */
public class ShellSqliteFunctionTreeItemValue extends RichTreeItemValue {

    /**
     * 构造函数节点值
     *
     * @param item 函数节点
     */
    public ShellSqliteFunctionTreeItemValue(ShellSqliteFunctionTreeItem item) {
        super(item);
        this.setRichMode(true);
    }

    @Override
    public ShellSqliteFunctionTreeItem item() {
        return (ShellSqliteFunctionTreeItem) super.item();
    }

    @Override
    public String name() {
        return this.item().functionName();
    }

    @Override
    public SVGGlyph graphic() {
        if (super.graphic() == null) {
            super.graphic(new FunctionSVGGlyph());
        }
        return super.graphic();
    }
}
