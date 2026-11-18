package cn.oyzh.easyshell.trees.oracle.table;

import cn.oyzh.fx.gui.svg.glyph.database.TableSVGGlyph;
import cn.oyzh.fx.gui.tree.view.RichTreeItemValue;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * Oracle数据库树表节点值
 *
 * @author oyzh
 * @since 2025-11-06
 */
public class ShellOracleTableTreeItemValue extends RichTreeItemValue {

    /**
     * 构造Oracle数据库树表节点值
     *
     * @param item 表节点
     */
    public ShellOracleTableTreeItemValue(ShellOracleTableTreeItem item) {
        super(item);
        this.setRichMode(true);
    }

    @Override
    public ShellOracleTableTreeItem item() {
        return (ShellOracleTableTreeItem) super.item();
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
