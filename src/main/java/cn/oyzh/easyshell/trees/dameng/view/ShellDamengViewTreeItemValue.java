package cn.oyzh.easyshell.trees.dameng.view;

import cn.oyzh.fx.gui.svg.glyph.database.ViewSVGGlyph;
import cn.oyzh.fx.gui.tree.view.RichTreeItemValue;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * 达梦数据库树视图节点值
 *
 * @author oyzh
 * @since 2025-11-06
 */
public class ShellDamengViewTreeItemValue extends RichTreeItemValue {

    /**
     * 构造达梦数据库树视图节点值
     *
     * @param item 视图节点
     */
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
