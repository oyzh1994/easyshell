package cn.oyzh.easyshell.trees.mysql.function;

import cn.oyzh.fx.gui.svg.glyph.database.FunctionSVGGlyph;
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
public class MysqlFunctionsTreeItemValue extends RichTreeItemValue {

    public MysqlFunctionsTreeItemValue(MysqlFunctionsTreeItem item) {
        super(item);
    }

    @Override
    public MysqlFunctionsTreeItem item() {
        return (MysqlFunctionsTreeItem) super.item();
    }

    @Override
    public String name() {
        return I18nHelper.function();
    }

    @Override
    public SVGGlyph graphic() {
        if (this.graphic == null) {
            this.graphic = new FunctionSVGGlyph("12");
            this.graphic.disableTheme();
        }
        return this.graphic;
    }

    @Override
    public Color graphicColor() {
        if (!this.item.isChildEmpty()) {
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
