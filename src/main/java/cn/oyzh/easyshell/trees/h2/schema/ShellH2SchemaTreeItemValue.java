package cn.oyzh.easyshell.trees.h2.schema;

import cn.oyzh.fx.gui.svg.glyph.database.SchemaSVGGlyph;
import cn.oyzh.fx.gui.tree.view.RichTreeItemValue;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import javafx.scene.paint.Color;

/**
 * H2 模式节点值。
 *
 * @author oyzh
 * @since 2026-11-28
 */
public class ShellH2SchemaTreeItemValue extends RichTreeItemValue {

    public ShellH2SchemaTreeItemValue(ShellH2SchemaTreeItem item) {
        super(item);
    }

    @Override
    public ShellH2SchemaTreeItem item() {
        return (ShellH2SchemaTreeItem) super.item();
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
        return this.item().isChildEmpty() ? super.graphicColor() : Color.GREEN;
    }
}
