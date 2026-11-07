package cn.oyzh.easyshell.trees.valkey.database;

import cn.oyzh.fx.gui.svg.glyph.database.DatabaseSVGGlyph;
import cn.oyzh.fx.gui.tree.view.RichTreeItemValue;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * Valkey DB值
 *
 * @author oyzh
 * @since 2025-09-01
 */
public class ShellValkeyDatabaseTreeItemValue extends RichTreeItemValue {

    /**
     * 构造redis数据库节点值
     *
     * @param item 数据库节点
     */
    public ShellValkeyDatabaseTreeItemValue(ShellValkeyDatabaseTreeItem item) {
        super(item);
        this.setRichMode(true);
    }

    @Override
    public ShellValkeyDatabaseTreeItem item() {
        return (ShellValkeyDatabaseTreeItem) super.item();
    }

    @Override
    public String name() {
        return this.item().value();
    }

    @Override
    public SVGGlyph graphic() {
        if (super.graphic() == null) {
            super.graphic(new DatabaseSVGGlyph());
            super.graphic().disableTheme();
        }
        return super.graphic();
    }

}
