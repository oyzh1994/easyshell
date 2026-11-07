package cn.oyzh.easyshell.trees.redis.database;

import cn.oyzh.fx.gui.svg.glyph.database.DatabaseSVGGlyph;
import cn.oyzh.fx.gui.tree.view.RichTreeItemValue;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * Redis DB值
 *
 * @author oyzh
 * @since 2025-09-01
 */
public class ShellRedisDatabaseTreeItemValue extends RichTreeItemValue {

    /**
     * 构造redis数据库节点值
     *
     * @param item 数据库节点
     */
    public ShellRedisDatabaseTreeItemValue(ShellRedisDatabaseTreeItem item) {
        super(item);
        this.setRichMode(true);
    }

    @Override
    public ShellRedisDatabaseTreeItem item() {
        return (ShellRedisDatabaseTreeItem) super.item();
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
