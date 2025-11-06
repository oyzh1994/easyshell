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
public class MysqlQueryTreeItemValue extends RichTreeItemValue {

    public MysqlQueryTreeItemValue(MysqlQueryTreeItem item) {
        super(item);
    }

    @Override
    protected MysqlQueryTreeItem item() {
        return (MysqlQueryTreeItem) super.item();
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
