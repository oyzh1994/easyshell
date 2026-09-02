package cn.oyzh.easyshell.trees.dameng.view;

import cn.oyzh.fx.gui.svg.glyph.database.ViewSVGGlyph;
import cn.oyzh.fx.gui.tree.view.RichTreeItemValue;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * db树表节点值
 *
 * @author oyzh
 * @since 2023/12/22
 */
public class ShellDamengViewTreeItemValue extends RichTreeItemValue {

    public ShellDamengViewTreeItemValue(ShellDamengViewTreeItem item) {
        super(item);
    }

    @Override
    public ShellDamengViewTreeItem item() {
        return (ShellDamengViewTreeItem) super.item();
    }

    @Override
    public SVGGlyph graphic() {
        if (super.graphic() == null) {
            super.graphic(new ViewSVGGlyph());
        }
        return super.graphic();
    }

    @Override
    public String name() {
        return this.item().viewName();
    }
}
