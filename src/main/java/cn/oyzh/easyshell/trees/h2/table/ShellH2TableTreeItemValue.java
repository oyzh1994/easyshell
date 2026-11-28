package cn.oyzh.easyshell.trees.h2.table;

import cn.oyzh.fx.gui.svg.glyph.database.TableSVGGlyph;
import cn.oyzh.fx.gui.tree.view.RichTreeItemValue;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * H2表节点值
 *
 * @author oyzh
 * @since 2026-11-02
 */
public class ShellH2TableTreeItemValue extends RichTreeItemValue {

    /**
     * 构造表节点值
     *
     * @param item 表节点
     */
    public ShellH2TableTreeItemValue(ShellH2TableTreeItem item) {
        super(item);
        this.setRichMode(true);
    }

    @Override
    public ShellH2TableTreeItem item() {
        return (ShellH2TableTreeItem) super.item();
    }

    @Override
    public SVGGlyph graphic() {
        if (super.graphic() == null) {
            super.graphic(new TableSVGGlyph());
        }
        return super.graphic();
    }

    @Override
    public String name() {
        return this.item().tableName();
    }
}
