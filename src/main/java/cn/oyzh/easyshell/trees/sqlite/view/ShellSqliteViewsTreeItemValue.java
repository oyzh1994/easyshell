package cn.oyzh.easyshell.trees.sqlite.view;

import cn.oyzh.fx.gui.svg.glyph.database.ViewSVGGlyph;
import cn.oyzh.fx.gui.tree.view.RichTreeItemValue;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.i18n.I18nHelper;
import javafx.scene.paint.Color;

/**
 * SQLite视图类型节点值
 *
 * @author oyzh
 * @since 2026-11-02
 */
public class ShellSqliteViewsTreeItemValue extends RichTreeItemValue {

    /**
     * 构造视图类型节点值
     *
     * @param item 视图类型节点
     */
    public ShellSqliteViewsTreeItemValue(ShellSqliteViewsTreeItem item) {
        super(item);
    }

    @Override
    public ShellSqliteViewsTreeItem item() {
        return (ShellSqliteViewsTreeItem) super.item();
    }

    @Override
    public String name() {
        return I18nHelper.view();
    }

    @Override
    public SVGGlyph graphic() {
        if (super.graphic() == null) {
            super.graphic(new ViewSVGGlyph());
            super.graphic().disableTheme();
        }
        return super.graphic();
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

    @Override
    public Color graphicColor() {
        if (!this.item().isChildEmpty()) {
            return Color.GREEN;
        }
        return super.graphicColor();
    }
}
