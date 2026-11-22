package cn.oyzh.easyshell.trees.sqlserver.procedure;

import cn.oyzh.fx.gui.svg.glyph.database.ProcedureSVGGlyph;
import cn.oyzh.fx.gui.tree.view.RichTreeItemValue;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.i18n.I18nHelper;
import javafx.scene.paint.Color;

/**
 * SqlServer数据库树过程类型节点值
 *
 * @author oyzh
 * @since 2025-11-06
 */
public class ShellSqlServerProceduresTreeItemValue extends RichTreeItemValue {

    /**
     * 构造SqlServer数据库树过程类型节点值
     *
     * @param item 过程类型节点
     */
    public ShellSqlServerProceduresTreeItemValue(ShellSqlServerProceduresTreeItem item) {
        super(item);
    }

    @Override
    public ShellSqlServerProceduresTreeItem item() {
        return (ShellSqlServerProceduresTreeItem) super.item();
    }

    @Override
    public String name() {
        return I18nHelper.procedure();
    }

    @Override
    public SVGGlyph graphic() {
        if (super.graphic() == null) {
            super.graphic(new ProcedureSVGGlyph());
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
        Integer size = this.item().getCachedProcedureSize();
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
