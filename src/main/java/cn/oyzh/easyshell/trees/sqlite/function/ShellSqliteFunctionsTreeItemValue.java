package cn.oyzh.easyshell.trees.sqlite.function;

import cn.oyzh.fx.gui.svg.glyph.database.FunctionSVGGlyph;
import cn.oyzh.fx.gui.tree.view.RichTreeItemValue;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.i18n.I18nHelper;
import javafx.scene.paint.Color;

/**
 * SQLite函数类型节点值
 *
 * @author oyzh
 * @since 2026-11-02
 */
public class ShellSqliteFunctionsTreeItemValue extends RichTreeItemValue {

    /**
     * 构造函数类型节点值
     *
     * @param item 函数类型节点
     */
    public ShellSqliteFunctionsTreeItemValue(ShellSqliteFunctionsTreeItem item) {
        super(item);
    }

    @Override
    public ShellSqliteFunctionsTreeItem item() {
        return (ShellSqliteFunctionsTreeItem) super.item();
    }

    @Override
    public String name() {
        return I18nHelper.function();
    }

    @Override
    public SVGGlyph graphic() {
        if (super.graphic() == null) {
            super.graphic(new FunctionSVGGlyph());
            super.graphic().disableTheme();
        }
        return super.graphic();
    }

    @Override
    public String extra() {
        Integer size = this.item().getFunctionSize();
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
