package cn.oyzh.easyshell.trees.dameng.table;

import cn.oyzh.fx.gui.svg.glyph.database.TableSVGGlyph;
import cn.oyzh.fx.gui.tree.view.RichTreeItemValue;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * 达梦数据库树表节点值
 *
 * @author oyzh
 * @since 2025-11-06
 */
public class ShellDamengTableTreeItemValue extends RichTreeItemValue {

    /**
     * 构造达梦数据库树表节点值
     *
     * @param item 表节点
     */
    public ShellDamengTableTreeItemValue(ShellDamengTableTreeItem item) {
        super(item);
    }

    @Override
    public ShellDamengTableTreeItem item() {
        return (ShellDamengTableTreeItem) super.item();
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
