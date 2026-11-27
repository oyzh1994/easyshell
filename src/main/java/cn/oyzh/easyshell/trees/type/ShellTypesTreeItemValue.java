package cn.oyzh.easyshell.trees.type;

import cn.oyzh.easyshell.util.ShellI18nHelper;
import cn.oyzh.fx.gui.svg.glyph.database.TypeSVGGlyph;
import cn.oyzh.fx.gui.tree.view.RichTreeItemValue;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import javafx.scene.paint.Color;

/**
 * 数据库树类型（自定义类型）类型节点值
 *
 * @author oyzh
 * @since 2026-11-28
 */
public class ShellTypesTreeItemValue extends RichTreeItemValue {

    /**
     * 构造数据库树类型节点值
     *
     * @param item 类型节点
     */
    public ShellTypesTreeItemValue(ShellTypesTreeItem item) {
        super(item);
    }

    @Override
    public ShellTypesTreeItem item() {
        return (ShellTypesTreeItem) super.item();
    }

    @Override
    public String name() {
        return ShellI18nHelper.types();
    }

    @Override
    public SVGGlyph graphic() {
        if (super.graphic() == null) {
            super.graphic(new TypeSVGGlyph());
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
        Integer size = this.item().getTypeSize();
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
