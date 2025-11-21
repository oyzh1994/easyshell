package cn.oyzh.easyshell.trees.mysql.procedure;

import cn.oyzh.fx.gui.svg.glyph.database.ProcedureSVGGlyph;
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
public class ShellMysqlProceduresTreeItemValue extends RichTreeItemValue {

    public ShellMysqlProceduresTreeItemValue(ShellMysqlProceduresTreeItem item) {
        super(item);
    }

    @Override
    public ShellMysqlProceduresTreeItem item() {
        return (ShellMysqlProceduresTreeItem) super.item();
    }

    @Override
    public String name() {
        return I18nHelper.procedure();
    }

    @Override
    public SVGGlyph graphic() {
        if (this.graphic == null) {
            this.graphic = new ProcedureSVGGlyph("12");
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
        Integer size = this.item().getProcedureSize();
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
