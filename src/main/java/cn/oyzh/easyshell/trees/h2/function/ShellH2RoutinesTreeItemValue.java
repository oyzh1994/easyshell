package cn.oyzh.easyshell.trees.h2.function;

import cn.oyzh.fx.gui.svg.glyph.database.FunctionSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.database.ProcedureSVGGlyph;
import cn.oyzh.fx.gui.tree.view.RichTreeItemValue;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.i18n.I18nHelper;
import javafx.scene.paint.Color;

/**
 * H2 例程分组节点值。
 */
public class ShellH2RoutinesTreeItemValue extends RichTreeItemValue {

    public ShellH2RoutinesTreeItemValue(ShellH2RoutinesTreeItem item) {
        super(item);
    }

    @Override
    public ShellH2RoutinesTreeItem item() {
        return (ShellH2RoutinesTreeItem) super.item();
    }

    @Override
    public String name() {
        return this.item().kind() == H2RoutineKind.FUNCTION
                ? I18nHelper.function() : I18nHelper.procedure();
    }

    @Override
    public SVGGlyph graphic() {
        if (super.graphic() == null) {
            super.graphic(this.item().kind() == H2RoutineKind.FUNCTION
                    ? new FunctionSVGGlyph() : new ProcedureSVGGlyph());
            super.graphic().disableTheme();
        }
        return super.graphic();
    }

    @Override
    public String extra() {
        Integer size = this.item().getRoutineSize();
        return size == null ? super.extra() : " (" + size + ")";
    }

    @Override
    public Color extraColor() {
        return Color.valueOf("#228B22");
    }

    @Override
    public Color graphicColor() {
        return this.item().isChildEmpty() ? super.graphicColor() : Color.GREEN;
    }
}
