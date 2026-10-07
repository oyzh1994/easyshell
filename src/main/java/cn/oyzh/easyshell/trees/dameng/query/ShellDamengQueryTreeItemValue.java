package cn.oyzh.easyshell.trees.dameng.query;

import cn.oyzh.fx.gui.svg.glyph.QuerySVGGlyph;
import cn.oyzh.fx.gui.tree.view.RichTreeItemValue;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * 达梦数据库树查询节点值
 *
 * @author oyzh
 * @since 2023/12/22
 */
public class ShellDamengQueryTreeItemValue extends RichTreeItemValue {

    /**
     * 构造达梦数据库树查询节点值
     *
     * @param item 查询节点
     */
    public ShellDamengQueryTreeItemValue(ShellDamengQueryTreeItem item) {
        super(item);
    }

    @Override
    public ShellDamengQueryTreeItem item() {
        return (ShellDamengQueryTreeItem) super.item();
    }

    @Override
    public SVGGlyph graphic() {
        if (super.graphic() == null) {
            super.graphic(new QuerySVGGlyph());
        }
        return super.graphic();
    }

    @Override
    public String name() {
        return this.item().queryName();
    }
}
