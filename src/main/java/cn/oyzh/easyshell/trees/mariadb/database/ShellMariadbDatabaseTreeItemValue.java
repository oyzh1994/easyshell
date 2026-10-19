package cn.oyzh.easyshell.trees.mariadb.database;

import cn.oyzh.fx.gui.svg.glyph.database.DatabaseSVGGlyph;
import cn.oyzh.fx.gui.tree.view.RichTreeItemValue;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import javafx.scene.paint.Color;

/**
 * MariaDB数据库节点值
 *
 * @author oyzh
 * @since 2026-10-09
 */
public class ShellMariadbDatabaseTreeItemValue extends RichTreeItemValue {

    /**
     * 构造数据库节点值
     *
     * @param item 数据库节点
     */
    public ShellMariadbDatabaseTreeItemValue(ShellMariadbDatabaseTreeItem item) {
        super(item);
        this.setRichMode(true);
    }

    @Override
    public ShellMariadbDatabaseTreeItem item() {
        return (ShellMariadbDatabaseTreeItem) super.item();
    }

    @Override
    public String name() {
        return this.item().dbName();
    }

    @Override
    public SVGGlyph graphic() {
        if (super.graphic() == null) {
            super.graphic(new DatabaseSVGGlyph());
            super.graphic().disableTheme();
        }
        return super.graphic();
    }

    @Override
    public Color graphicColor() {
        if (!this.item().isChildEmpty()) {
            return Color.GREEN;
        }
        return super.graphicColor();
    }
}
