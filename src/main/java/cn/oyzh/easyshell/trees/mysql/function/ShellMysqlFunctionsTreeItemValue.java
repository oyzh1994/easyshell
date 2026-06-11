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
public class ShellMysqlFunctionsTreeItemValue extends RichTreeItemValue {

    public ShellMysqlFunctionsTreeItemValue(ShellMysqlFunctionsTreeItem item) {
        super(item);
        super.setRichMode(true);
    }

    @Override
    public ShellMysqlFunctionsTreeItem item() {
        return (ShellMysqlFunctionsTreeItem) super.item();
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
//
//    @Override
//    public Color graphicColor() {
//        if (!this.item().isChildEmpty()) {
//            return Color.GREEN;
//        }
//        return super.graphicColor();
//    }

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
}
