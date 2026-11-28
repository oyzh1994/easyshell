package cn.oyzh.easyshell.trees.h2.function;

import cn.oyzh.fx.gui.svg.glyph.database.FunctionSVGGlyph;
import cn.oyzh.fx.gui.svg.glyph.database.ProcedureSVGGlyph;
import cn.oyzh.fx.gui.tree.view.RichTreeItemValue;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * H2 例程节点值。
 */
public class ShellH2RoutineTreeItemValue extends RichTreeItemValue {

    public ShellH2RoutineTreeItemValue(ShellH2RoutineTreeItem item) {
        super(item);
    }

    @Override
    public ShellH2RoutineTreeItem item() {
        return (ShellH2RoutineTreeItem) super.item();
    }

    @Override
    public String name() {
        return this.item().routineName();
    }

    @Override
    public SVGGlyph graphic() {
        if (super.graphic() == null) {
            super.graphic(this.item().value().isProcedure()
                    ? new ProcedureSVGGlyph() : new FunctionSVGGlyph());
            super.graphic().disableTheme();
        }
        return super.graphic();
    }
}
