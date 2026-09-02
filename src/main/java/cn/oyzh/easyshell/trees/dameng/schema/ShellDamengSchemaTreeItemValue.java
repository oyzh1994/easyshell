package cn.oyzh.easyshell.trees.dameng.schema;

import cn.oyzh.fx.gui.svg.glyph.database.SchemaSVGGlyph;
import cn.oyzh.fx.gui.tree.view.RichTreeItemValue;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import javafx.scene.paint.Color;

/**
 * database值
 *
 * @author oyzh
 * @since 2023/12/20
 */
public class ShellDamengSchemaTreeItemValue extends RichTreeItemValue {

    public ShellDamengSchemaTreeItemValue(ShellDamengSchemaTreeItem item) {
        super(item);
    }

    @Override
    public ShellDamengSchemaTreeItem item() {
        return (ShellDamengSchemaTreeItem) super.item();
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
