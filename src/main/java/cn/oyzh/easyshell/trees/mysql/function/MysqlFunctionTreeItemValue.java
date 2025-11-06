package cn.oyzh.easyshell.trees.mysql.function;

import cn.oyzh.fx.gui.svg.glyph.database.FunctionSVGGlyph;
import cn.oyzh.fx.gui.tree.view.RichTreeItemValue;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * db树表节点值
 *
 * @author oyzh
 * @since 2023/12/22
 */
public class MysqlFunctionTreeItemValue extends RichTreeItemValue {

    public MysqlFunctionTreeItemValue(MysqlFunctionTreeItem item) {
        super(item);
    }

    @Override
    protected MysqlFunctionTreeItem item() {
        return (MysqlFunctionTreeItem) super.item();
    }

    @Override
    public SVGGlyph graphic() {
        if (this.graphic == null) {
            this.graphic = new FunctionSVGGlyph("12");
        }
        return super.graphic();
    }

    @Override
    public String name() {
        return this.item().functionName();
    }
}
