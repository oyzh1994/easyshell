package cn.oyzh.easyshell.trees.dameng.table;

import cn.oyzh.easyshell.trees.dameng.table.DamengTableTreeItem;
import cn.oyzh.fx.gui.svg.glyph.database.TableSVGGlyph;
import cn.oyzh.fx.gui.tree.view.RichTreeItemValue;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * db树表节点值
 *
 * @author oyzh
 * @since 2023/12/22
 */
public class DamengTableTreeItemValue extends RichTreeItemValue {

    public DamengTableTreeItemValue(DamengTableTreeItem item) {
        super(item);
    }

    @Override
    public DamengTableTreeItem item() {
        return (DamengTableTreeItem) super.item();
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
