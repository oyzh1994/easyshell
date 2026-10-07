package cn.oyzh.easyshell.trees.dameng.function;

import cn.oyzh.fx.gui.svg.glyph.database.FunctionSVGGlyph;
import cn.oyzh.fx.gui.tree.view.RichTreeItemValue;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.i18n.I18nHelper;
import javafx.scene.paint.Color;

/**
 * 达梦数据库树函数类型节点值
 *
 * @author oyzh
 * @since 2025-11-06
 */
public class ShellDamengFunctionsTreeItemValue extends RichTreeItemValue {

    /**
     * 构造达梦数据库树函数类型节点值
     *
     * @param item 函数类型节点
     */
    public ShellDamengFunctionsTreeItemValue(ShellDamengFunctionsTreeItem item) {
        super(item);
    }

    @Override
    public ShellDamengFunctionsTreeItem item() {
        return (ShellDamengFunctionsTreeItem) super.item();
    }

    @Override
    public String name() {
        return I18nHelper.function();
    }

    @Override
    public SVGGlyph graphic() {
        if (super.graphic() == null) {
            super.graphic(new FunctionSVGGlyph()) ;
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
        Integer size = this.item().functionSize();
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
