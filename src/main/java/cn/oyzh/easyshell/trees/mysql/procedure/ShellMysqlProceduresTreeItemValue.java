package cn.oyzh.easyshell.trees.mysql.procedure;

import cn.oyzh.fx.gui.svg.glyph.database.ProcedureSVGGlyph;
import cn.oyzh.fx.gui.tree.view.RichTreeItemValue;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.i18n.I18nHelper;
import javafx.scene.paint.Color;

/**
 * mysql过程类型节点值
 *
 * @author oyzh
 * @since 2025-11-06
 */
public class ShellMysqlProceduresTreeItemValue extends RichTreeItemValue {

    /**
     * 构造过程类型节点值
     *
     * @param item 过程类型节点
     */
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
        if (super.graphic() == null) {
            super.graphic(new ProcedureSVGGlyph());
            super.graphic().disableTheme();
        }
        return super.graphic();
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
