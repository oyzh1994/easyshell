package cn.oyzh.easyshell.trees.h2.view;

import cn.oyzh.fx.gui.svg.glyph.database.ViewSVGGlyph;
import cn.oyzh.fx.gui.tree.view.RichTreeItemValue;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * H2视图节点值
 *
 * @author oyzh
 * @since 2026-11-02
 */
public class ShellH2ViewTreeItemValue extends RichTreeItemValue {

    /**
     * 构造视图节点值
     *
     * @param item 视图节点
     */
    public ShellH2ViewTreeItemValue(ShellH2ViewTreeItem item) {
        super(item);
        this.setRichMode(true);
    }

    @Override
    public ShellH2ViewTreeItem item() {
        return (ShellH2ViewTreeItem) super.item();
    }

    @Override
    public String name() {
        return this.item().viewName();
    }

    @Override
    public SVGGlyph graphic() {
        if (super.graphic() == null) {
            super.graphic(new ViewSVGGlyph());
        }
        return super.graphic();
    }
}
