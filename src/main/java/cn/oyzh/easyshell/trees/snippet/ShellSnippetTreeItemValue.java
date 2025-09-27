package cn.oyzh.easyshell.trees.snippet;

import cn.oyzh.fx.gui.svg.glyph.SnippetSVGGlyph;
import cn.oyzh.fx.gui.tree.view.RichTreeItemValue;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import javafx.scene.paint.Color;

/**
 * shell片段节点值
 *
 * @author oyzh
 * @since 2025/06/22
 */
public class ShellSnippetTreeItemValue extends RichTreeItemValue {

    public ShellSnippetTreeItemValue(ShellSnippetTreeItem item) {
        super(item);
        this.setRichMode(true);
    }

    @Override
    protected ShellSnippetTreeItem item() {
        return (ShellSnippetTreeItem) super.item();
    }

    @Override
    public String name() {
        return this.item().value().getName();
    }

    @Override
    public SVGGlyph graphic() {
        if (this.graphic == null) {
            this.graphic = new SnippetSVGGlyph("12");
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
