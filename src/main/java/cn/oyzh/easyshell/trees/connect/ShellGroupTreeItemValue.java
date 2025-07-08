package cn.oyzh.easyshell.trees.connect;

import cn.oyzh.fx.gui.svg.glyph.GroupSVGGlyph;
import cn.oyzh.fx.gui.tree.view.RichTreeItemValue;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import javafx.scene.paint.Color;

/**
 * shell树group值
 *
 * @author oyzh
 * @since 2025/4/7
 */
public class ShellGroupTreeItemValue extends RichTreeItemValue {

    public ShellGroupTreeItemValue(ShellGroupTreeItem item) {
        super(item);
        this.setRichMode(true);
    }

    @Override
    protected ShellGroupTreeItem item() {
        return (ShellGroupTreeItem) super.item();
    }

    @Override
    public String name() {
        return this.item().value().getName();
    }

    @Override
    public SVGGlyph graphic() {
        if (this.graphic == null) {
            this.graphic = new GroupSVGGlyph("10");
        }
        return super.graphic();
    }

    @Override
    public Color graphicColor() {
        if (this.item().isChildEmpty()) {
            return super.graphicColor();
        }
        return Color.DEEPSKYBLUE;
    }

    @Override
    public String extra() {
        if (this.item().isChildEmpty()) {
            return super.extra();
        }
        return "(" + this.item().getChildrenSize() + ")";
    }

    @Override
    public Color extraColor() {
        return Color.DARKGREY;
    }
}
