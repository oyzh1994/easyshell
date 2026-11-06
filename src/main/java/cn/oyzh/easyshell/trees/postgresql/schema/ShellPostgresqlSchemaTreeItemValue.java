package cn.oyzh.easyshell.trees.postgresql.schema;

import cn.oyzh.fx.gui.svg.glyph.database.SchemaSVGGlyph;
import cn.oyzh.fx.gui.tree.view.RichTreeItemValue;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import javafx.scene.paint.Color;

/**
 * PostgreSQL数据库树模式节点值
 *
 * @author oyzh
 * @since 2025-11-06
 */
public class ShellPostgresqlSchemaTreeItemValue extends RichTreeItemValue {

    /**
     * 构造PostgreSQL数据库树模式节点值
     *
     * @param item 模式节点
     */
    public ShellPostgresqlSchemaTreeItemValue(ShellPostgresqlSchemaTreeItem item) {
        super(item);
    }

    @Override
    public ShellPostgresqlSchemaTreeItem item() {
        return (ShellPostgresqlSchemaTreeItem) super.item();
    }

    @Override
    public String name() {
        return this.item().schema();
    }

    @Override
    public SVGGlyph graphic() {
        if (super.graphic() == null) {
            super.graphic(new SchemaSVGGlyph());
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
