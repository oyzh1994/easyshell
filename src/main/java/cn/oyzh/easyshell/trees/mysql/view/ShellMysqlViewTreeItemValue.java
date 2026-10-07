package cn.oyzh.easyshell.trees.mysql.view;

import cn.oyzh.fx.gui.svg.glyph.database.ViewSVGGlyph;
import cn.oyzh.fx.gui.tree.view.RichTreeItemValue;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * mysql视图节点值
 *
 * @author oyzh
 * @since 2025-11-06
 */
public class ShellMysqlViewTreeItemValue extends RichTreeItemValue {

    /**
     * 构造视图节点值
     *
     * @param item 视图节点
     */
    public ShellMysqlViewTreeItemValue(ShellMysqlViewTreeItem item) {
        super(item);
        this.setRichMode(true);
    }

    @Override
    public ShellMysqlViewTreeItem item() {
        return (ShellMysqlViewTreeItem) super.item();
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
