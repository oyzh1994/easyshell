package cn.oyzh.easyshell.trees.mysql.query;

import cn.oyzh.fx.gui.svg.glyph.QuerySVGGlyph;
import cn.oyzh.fx.gui.tree.view.RichTreeItemValue;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * db树表节点值
 *
 * @author oyzh
 * @since 2023/12/22
 */
public class ShellMysqlQueryTreeItemValue extends RichTreeItemValue {

    public ShellMysqlQueryTreeItemValue(ShellMysqlQueryTreeItem item) {
        super(item);
    }

    @Override
    protected ShellMysqlQueryTreeItem item() {
        return (ShellMysqlQueryTreeItem) super.item();
    }

    @Override
    public SVGGlyph graphic() {
        if (this.graphic == null) {
            this.graphic  = new QuerySVGGlyph("12");
        }
        return super.graphic();
    }

    @Override
    public String name() {
        return this.item().queryName();
    }
}
