package cn.oyzh.easyshell.trees.query;

import cn.oyzh.fx.gui.svg.glyph.QuerySVGGlyph;
import cn.oyzh.fx.gui.tree.view.RichTreeItemValue;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import javafx.scene.paint.Color;

/**
 * shell查询节点值
 *
 * @author oyzh
 * @since 2025/06/22
 */
public class ShellQueryTreeItemValue extends RichTreeItemValue {

    public ShellQueryTreeItemValue(ShellQueryTreeItem item) {
        super(item);
        this.setRichMode(true);
    }

    @Override
    protected ShellQueryTreeItem item() {
        return (ShellQueryTreeItem) super.item();
    }

    @Override
    public String name() {
        return this.item().value().getName();
    }

    @Override
    public SVGGlyph graphic() {
        if (this.graphic == null) {
            this.graphic = new QuerySVGGlyph("12");
        }
        return super.graphic();
    }

    @Override
    public String extra() {
        if (this.item().isUnsaved()) {
            return " *";
        }
        return super.extra();
    }

    @Override
    public Color extraColor() {
        if (this.item().isUnsaved()) {
            return Color.RED;
        }
        return super.extraColor();
    }
}
