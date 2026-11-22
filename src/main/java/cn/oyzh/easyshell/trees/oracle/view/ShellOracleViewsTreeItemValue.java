package cn.oyzh.easyshell.trees.oracle.view;

import cn.oyzh.fx.gui.svg.glyph.database.ViewSVGGlyph;
import cn.oyzh.fx.gui.tree.view.RichTreeItemValue;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.i18n.I18nHelper;
import javafx.scene.paint.Color;

/**
 * Oracle数据库树视图类型节点值
 *
 * @author oyzh
 * @since 2025-11-06
 */
public class ShellOracleViewsTreeItemValue extends RichTreeItemValue {

    /**
     * 构造Oracle数据库树视图类型节点值
     *
     * @param item 视图类型节点
     */
    public ShellOracleViewsTreeItemValue(ShellOracleViewsTreeItem item) {
        super(item);
    }

    @Override
    public String name() {
        return I18nHelper.view();
    }

    @Override
    public ShellOracleViewsTreeItem item() {
        return (ShellOracleViewsTreeItem) super.item();
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
        Integer size = this.item().getCachedViewSize();
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
