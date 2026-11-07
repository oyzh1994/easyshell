package cn.oyzh.easyshell.trees.valkey.query;

import cn.oyzh.fx.gui.svg.glyph.QuerySVGGlyph;
import cn.oyzh.fx.gui.tree.view.RichTreeItemValue;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * redis查询节点值
 *
 * @author oyzh
 * @since 2026-11-07
 */
public class ShellValkeyQueryTreeItemValue extends RichTreeItemValue {

    /**
     * 构造查询节点值
     *
     * @param item 查询节点
     */
    public ShellValkeyQueryTreeItemValue(ShellValkeyQueryTreeItem item) {
        super(item);
        this.setRichMode(true);
    }

    @Override
    public ShellValkeyQueryTreeItem item() {
        return (ShellValkeyQueryTreeItem) super.item();
    }

    @Override
    public String name() {
        return this.item().queryName();
    }

    @Override
    public SVGGlyph graphic() {
        if (super.graphic() == null) {
            super.graphic(new QuerySVGGlyph());
        }
        return super.graphic();
    }
}
