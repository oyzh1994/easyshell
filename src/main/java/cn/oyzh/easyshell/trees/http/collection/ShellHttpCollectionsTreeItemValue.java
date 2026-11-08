package cn.oyzh.easyshell.trees.http.collection;

import cn.oyzh.fx.gui.svg.glyph.file.FolderSVGGlyph;
import cn.oyzh.fx.gui.tree.view.RichTreeItemValue;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.i18n.I18nHelper;
import javafx.scene.paint.Color;

/**
 * HTTP 请求集合分类节点值
 *
 * @author oyzh
 * @since 2026-11-08
 */
public class ShellHttpCollectionsTreeItemValue extends RichTreeItemValue {

    /**
     * 构造集合分类节点值
     *
     * @param item 集合分类节点
     */
    public ShellHttpCollectionsTreeItemValue(ShellHttpCollectionsTreeItem item) {
        super(item);
        this.setRichMode(true);
    }

    @Override
    public ShellHttpCollectionsTreeItem item() {
        return (ShellHttpCollectionsTreeItem) super.item();
    }

    @Override
    public String name() {
        return I18nHelper.collections();
    }

    @Override
    public String extra() {
        return "(" + this.item().workspace().requestCount() + ")";
    }

    @Override
    public Color extraColor() {
        return Color.FORESTGREEN;
    }

    @Override
    public SVGGlyph graphic() {
        if (super.graphic() == null) {
            super.graphic(new FolderSVGGlyph());
            super.graphic().disableTheme();
        }
        return super.graphic();
    }

    @Override
    public Color graphicColor() {
        if (this.item().workspace().requestCount() > 0) {
            return Color.FORESTGREEN;
        }
        return super.graphicColor();
    }
}
