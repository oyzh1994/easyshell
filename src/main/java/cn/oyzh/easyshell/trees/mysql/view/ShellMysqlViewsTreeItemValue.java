package cn.oyzh.easyshell.trees.mysql.view;

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
public class ShellMysqlViewsTreeItemValue extends RichTreeItemValue {

    public ShellMysqlViewsTreeItemValue(ShellMysqlViewsTreeItem item) {
        super(item);
    }

    @Override
    public String name() {
        return I18nHelper.view();
    }

    @Override
    protected ShellMysqlViewsTreeItem item() {
        return (ShellMysqlViewsTreeItem) super.item();
    }

    @Override
    public SVGGlyph graphic() {
        if (this.graphic == null) {
            this.graphic = new ViewSVGGlyph("12");
            this.graphic.disableTheme();
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
        Integer size = this.item().getViewSize();
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
