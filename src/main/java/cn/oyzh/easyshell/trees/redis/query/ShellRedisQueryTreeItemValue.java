package cn.oyzh.easyshell.trees.redis.query;

import cn.oyzh.fx.gui.svg.glyph.QuerySVGGlyph;
import cn.oyzh.fx.gui.tree.view.RichTreeItemValue;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * shell代码查询节点值
 *
 * @author oyzh
 * @since 2025/06/22
 */
public class ShellRedisQueryTreeItemValue extends RichTreeItemValue {

    public ShellRedisQueryTreeItemValue(ShellRedisQueryTreeItem item) {
        super(item);
        this.setRichMode(true);
    }

    @Override
    protected ShellRedisQueryTreeItem item() {
        return (ShellRedisQueryTreeItem) super.item();
    }

    @Override
    public String name() {
        return this.item().value().getName();
    }

    @Override
    public SVGGlyph graphic() {
        if (this.graphic == null) {
            this.graphic = new QuerySVGGlyph("12");
        }
        return super.graphic();
    }
}
