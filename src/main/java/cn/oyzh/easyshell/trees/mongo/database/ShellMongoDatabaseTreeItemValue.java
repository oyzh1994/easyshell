package cn.oyzh.easyshell.trees.mongo.database;

import cn.oyzh.fx.gui.svg.glyph.database.DatabaseSVGGlyph;
import cn.oyzh.fx.gui.tree.view.RichTreeItemValue;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import javafx.scene.paint.Color;

/**
 * database值
 *
 * @author oyzh
 * @since 2023/12/20
 */
public class ShellMongoDatabaseTreeItemValue extends RichTreeItemValue {

    public ShellMongoDatabaseTreeItemValue(ShellMongoDatabaseTreeItem item) {
        super(item);
        super.setRichMode(true);
    }

    @Override
    public ShellMongoDatabaseTreeItem item() {
        return (ShellMongoDatabaseTreeItem) super.item();
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
