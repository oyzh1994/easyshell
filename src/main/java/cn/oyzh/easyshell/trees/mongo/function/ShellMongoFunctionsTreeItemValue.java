package cn.oyzh.easyshell.trees.mongo.function;

import cn.oyzh.fx.gui.svg.glyph.database.FunctionSVGGlyph;
import cn.oyzh.fx.gui.tree.view.RichTreeItemValue;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.i18n.I18nHelper;
import javafx.scene.paint.Color;

/**
 * mongodb树函数类型值
 *
 * @author oyzh
 * @since 2024/06/28
 */
public class ShellMongoFunctionsTreeItemValue extends RichTreeItemValue {

    /**
     * 构造函数类型节点值
     *
     * @param item 函数类型节点
     */
    public ShellMongoFunctionsTreeItemValue(ShellMongoFunctionsTreeItem item) {
        super(item);
    }

    @Override
    public ShellMongoFunctionsTreeItem item() {
        return (ShellMongoFunctionsTreeItem) super.item();
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
