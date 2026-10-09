package cn.oyzh.easyshell.trees.mariadb.query;

import cn.oyzh.fx.gui.svg.glyph.QuerySVGGlyph;
import cn.oyzh.fx.gui.tree.view.RichTreeItemValue;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * MariaDB查询节点值
 *
 * @author oyzh
 * @since 2026-10-09
 */
public class ShellMariadbQueryTreeItemValue extends RichTreeItemValue {

    /**
     * 构造查询节点值
     *
     * @param item 查询节点
     */
    public ShellMariadbQueryTreeItemValue(ShellMariadbQueryTreeItem item) {
        super(item);
        this.setRichMode(true);
    }

    @Override
    public ShellMariadbQueryTreeItem item() {
        return (ShellMariadbQueryTreeItem) super.item();
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
