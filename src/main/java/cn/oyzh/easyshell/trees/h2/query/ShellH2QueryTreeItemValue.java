package cn.oyzh.easyshell.trees.h2.query;

import cn.oyzh.fx.gui.svg.glyph.QuerySVGGlyph;
import cn.oyzh.fx.gui.tree.view.RichTreeItemValue;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * H2查询节点值
 *
 * @author oyzh
 * @since 2026-11-02
 */
public class ShellH2QueryTreeItemValue extends RichTreeItemValue {

    /**
     * 构造查询节点值
     *
     * @param item 查询节点
     */
    public ShellH2QueryTreeItemValue(ShellH2QueryTreeItem item) {
        super(item);
        this.setRichMode(true);
    }

    @Override
    public ShellH2QueryTreeItem item() {
        return (ShellH2QueryTreeItem) super.item();
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
