package cn.oyzh.easyshell.trees.mariadb.view;

import cn.oyzh.fx.gui.svg.glyph.database.ViewSVGGlyph;
import cn.oyzh.fx.gui.tree.view.RichTreeItemValue;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * MariaDB视图节点值
 *
 * @author oyzh
 * @since 2026-10-09
 */
public class ShellMariadbViewTreeItemValue extends RichTreeItemValue {

    /**
     * 构造视图节点值
     *
     * @param item 视图节点
     */
    public ShellMariadbViewTreeItemValue(ShellMariadbViewTreeItem item) {
        super(item);
        this.setRichMode(true);
    }

    @Override
    public ShellMariadbViewTreeItem item() {
        return (ShellMariadbViewTreeItem) super.item();
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
