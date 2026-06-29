package cn.oyzh.easyshell.trees.mongo.function;

import cn.oyzh.fx.gui.svg.glyph.database.FunctionSVGGlyph;
import cn.oyzh.fx.gui.tree.view.RichTreeItemValue;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * db树表节点值
 *
 * @author oyzh
 * @since 2023/12/22
 */
public class ShellMongoFunctionTreeItemValue extends RichTreeItemValue {

    public ShellMongoFunctionTreeItemValue(ShellMongoFunctionTreeItem item) {
        super(item);
        this.setRichMode(true);
    }

    @Override
    public ShellMongoFunctionTreeItem item() {
        return (ShellMongoFunctionTreeItem) super.item();
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
