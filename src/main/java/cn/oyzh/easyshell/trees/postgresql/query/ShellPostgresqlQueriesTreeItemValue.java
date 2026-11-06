package cn.oyzh.easyshell.trees.postgresql.query;

import cn.oyzh.fx.gui.svg.glyph.QuerySVGGlyph;
import cn.oyzh.fx.gui.tree.view.RichTreeItemValue;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.i18n.I18nHelper;
import javafx.scene.paint.Color;

/**
 * PostgreSQL数据库树查询类型节点值
 *
 * @author oyzh
 * @since 2025-11-06
 */
public class ShellPostgresqlQueriesTreeItemValue extends RichTreeItemValue {

    /**
     * 构造PostgreSQL数据库树查询类型节点值
     *
     * @param item 查询类型节点
     */
    public ShellPostgresqlQueriesTreeItemValue(ShellPostgresqlQueriesTreeItem item) {
        super(item);
    }

    @Override
    public ShellPostgresqlQueriesTreeItem item() {
        return (ShellPostgresqlQueriesTreeItem) super.item();
    }

    @Override
    public String name() {
        return I18nHelper.queries();
    }

    @Override
    public SVGGlyph graphic() {
        if (super.graphic() == null) {
            super.graphic(new QuerySVGGlyph())  ;
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
        Integer size = this.item().querySize();
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
