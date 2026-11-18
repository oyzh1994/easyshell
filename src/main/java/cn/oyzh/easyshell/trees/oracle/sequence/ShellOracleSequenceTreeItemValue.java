package cn.oyzh.easyshell.trees.oracle.sequence;

import cn.oyzh.fx.gui.svg.glyph.database.SequenceSVGGlyph;
import cn.oyzh.fx.gui.tree.view.RichTreeItemValue;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * Oracle数据库树序列节点值
 *
 * @author oyzh
 * @since 2025-11-06
 */
public class ShellOracleSequenceTreeItemValue extends RichTreeItemValue {

    /**
     * 构造Oracle数据库树序列节点值
     *
     * @param item 序列节点
     */
    public ShellOracleSequenceTreeItemValue(ShellOracleSequenceTreeItem item) {
        super(item);
        this.setRichMode(true);
    }

    @Override
    public ShellOracleSequenceTreeItem item() {
        return (ShellOracleSequenceTreeItem) super.item();
    }

    @Override
    public SVGGlyph graphic() {
        if (super.graphic() == null) {
            super.graphic(new SequenceSVGGlyph());
        }
        return super.graphic();
    }

    @Override
    public String name() {
        return this.item().sequenceName();
    }
}
