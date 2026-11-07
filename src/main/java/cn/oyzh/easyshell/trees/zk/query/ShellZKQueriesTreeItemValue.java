package cn.oyzh.easyshell.trees.zk.query;

import cn.oyzh.fx.gui.svg.glyph.QuerySVGGlyph;
import cn.oyzh.fx.gui.tree.view.RichTreeItemValue;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.i18n.I18nHelper;
import javafx.scene.paint.Color;

/**
 * zk查询分类节点值
 *
 * @author oyzh
 * @since 2026-11-07
 */
public class ShellZKQueriesTreeItemValue extends RichTreeItemValue {

    /**
     * 构造查询分类节点值
     *
     * @param item 查询分类节点
     */
    public ShellZKQueriesTreeItemValue(ShellZKQueriesTreeItem item) {
        super(item);
        this.setRichMode(true);
    }

    @Override
    public ShellZKQueriesTreeItem item() {
        return (ShellZKQueriesTreeItem) super.item();
    }

    @Override
    public String name() {
        return I18nHelper.queries();
    }

    @Override
    public SVGGlyph graphic() {
        if (super.graphic() == null) {
            super.graphic(new QuerySVGGlyph());
            super.graphic().disableTheme();
        }
        return super.graphic();
    }

    @Override
    public String extra() {
        Integer size = this.item().getQuerySize();
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
