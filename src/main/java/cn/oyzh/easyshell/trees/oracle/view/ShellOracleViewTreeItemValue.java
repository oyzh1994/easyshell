package cn.oyzh.easyshell.trees.oracle.view;

import cn.oyzh.fx.gui.svg.glyph.database.ViewSVGGlyph;
import cn.oyzh.fx.gui.tree.view.RichTreeItemValue;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * Oracle数据库树视图节点值
 *
 * @author oyzh
 * @since 2025-11-06
 */
public class ShellOracleViewTreeItemValue extends RichTreeItemValue {

    /**
     * 构造Oracle数据库树视图节点值
     *
     * @param item 视图节点
     */
    public ShellOracleViewTreeItemValue(ShellOracleViewTreeItem item) {
        super(item);
        this.setRichMode(true);
    }

    @Override
    public ShellOracleViewTreeItem item() {
        return (ShellOracleViewTreeItem) super.item();
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
