package cn.oyzh.easyshell.trees.dameng.view;

import cn.oyzh.fx.gui.svg.glyph.database.ViewSVGGlyph;
import cn.oyzh.fx.gui.tree.view.RichTreeItemValue;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.i18n.I18nHelper;
import javafx.scene.paint.Color;

/**
 * db树视图类型值
 *
 * @author oyzh
 * @since 2024/06/28
 */
public class ShellDamengViewsTreeItemValue extends RichTreeItemValue {

    public ShellDamengViewsTreeItemValue(ShellDamengViewsTreeItem item) {
        super(item);
    }

    @Override
    public String name() {
        return I18nHelper.view();
    }

    @Override
    public ShellDamengViewsTreeItem item() {
        return (ShellDamengViewsTreeItem) super.item();
    }

    @Override
    public SVGGlyph graphic() {
        if (super.graphic() == null) {
            super.graphic(new ViewSVGGlyph())  ;
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

    @Override
    public String extra() {
        Integer size = this.item().viewSize();
        if (size != null) {
            return " (" + size + ")";
        }
        return super.extra();
    }

    @Override
    public Color extraColor() {
        return Color.valueOf("#228B22");
    }
}
