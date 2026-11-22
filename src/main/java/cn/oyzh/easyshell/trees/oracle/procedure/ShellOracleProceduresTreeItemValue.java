package cn.oyzh.easyshell.trees.oracle.procedure;

import cn.oyzh.fx.gui.svg.glyph.database.ProcedureSVGGlyph;
import cn.oyzh.fx.gui.tree.view.RichTreeItemValue;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.i18n.I18nHelper;
import javafx.scene.paint.Color;

/**
 * Oracle数据库树过程类型节点值
 *
 * @author oyzh
 * @since 2025-11-06
 */
public class ShellOracleProceduresTreeItemValue extends RichTreeItemValue {

    /**
     * 构造Oracle数据库树过程类型节点值
     *
     * @param item 过程类型节点
     */
    public ShellOracleProceduresTreeItemValue(ShellOracleProceduresTreeItem item) {
        super(item);
    }

    @Override
    public ShellOracleProceduresTreeItem item() {
        return (ShellOracleProceduresTreeItem) super.item();
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
        return this.item().extraCount(this.item().getCachedProcedureSize());
    }

    @Override
    public Color extraColor() {
        return Color.valueOf("#228B22");
    }
}
