package cn.oyzh.easyshell.trees.index;

import cn.oyzh.fx.gui.svg.glyph.database.IndexSVGGlyph;
import cn.oyzh.fx.gui.tree.view.RichTreeItemValue;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.i18n.I18nResourceBundle;
import javafx.scene.paint.Color;

/**
 * 数据库树索引类型节点值
 *
 * @author oyzh
 * @since 2026-11-28
 */
public class ShellIndexesTreeItemValue extends RichTreeItemValue {

    /**
     * 构造数据库树索引类型节点值
     *
     * @param item 索引类型节点
     */
    public ShellIndexesTreeItemValue(ShellIndexesTreeItem item) {
        super(item);
    }

    @Override
    public ShellIndexesTreeItem item() {
        return (ShellIndexesTreeItem) super.item();
    }

    @Override
    public String name() {
        return I18nResourceBundle.i18nString("base.indexes");
    }

    @Override
    public SVGGlyph graphic() {
        if (super.graphic() == null) {
            super.graphic(new IndexSVGGlyph());
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
        Integer size = this.item().getIndexSize();
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
