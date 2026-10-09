package cn.oyzh.easyshell.trees.mariadb.table;

import cn.oyzh.fx.gui.svg.glyph.database.TableSVGGlyph;
import cn.oyzh.fx.gui.tree.view.RichTreeItemValue;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.i18n.I18nHelper;
import javafx.scene.paint.Color;

/**
 * MariaDB表类型节点值
 *
 * @author oyzh
 * @since 2026-10-09
 */
public class ShellMariadbTablesTreeItemValue extends RichTreeItemValue {

    /**
     * 构造表类型节点值
     *
     * @param item 表类型节点
     */
    public ShellMariadbTablesTreeItemValue(ShellMariadbTablesTreeItem item) {
        super(item);
    }

    @Override
    public ShellMariadbTablesTreeItem item() {
        return (ShellMariadbTablesTreeItem) super.item();
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

//    @Override
//    public Color graphicColor() {
//        if (!this.item().isChildEmpty()) {
//            return Color.GREEN;
//        }
//        return super.graphicColor();
//    }

    @Override
    public String extra() {
        Integer size = this.item().getTableSize();
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
