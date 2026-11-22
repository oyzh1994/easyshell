package cn.oyzh.easyshell.trees.sqlserver.table;

import cn.oyzh.fx.gui.svg.glyph.database.TableSVGGlyph;
import cn.oyzh.fx.gui.tree.view.RichTreeItemValue;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.i18n.I18nHelper;
import javafx.scene.paint.Color;

/**
 * SqlServer数据库树表类型节点值
 *
 * @author oyzh
 * @since 2025-11-06
 */
public class ShellSqlServerTablesTreeItemValue extends RichTreeItemValue {

    /**
     * 构造SqlServer数据库树表类型节点值
     *
     * @param item 表类型节点
     */
    public ShellSqlServerTablesTreeItemValue(ShellSqlServerTablesTreeItem item) {
        super(item);
    }

    @Override
    public ShellSqlServerTablesTreeItem item() {
        return (ShellSqlServerTablesTreeItem) super.item();
    }

    @Override
    public String name() {
        return I18nHelper.table();
    }

    @Override
    public SVGGlyph graphic() {
        if (super.graphic() == null) {
            super.graphic(new TableSVGGlyph());
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
        return this.item().extraCount(this.item().getCachedTableSize());
    }

    @Override
    public Color extraColor() {
        return Color.valueOf("#228B22");
    }
}
