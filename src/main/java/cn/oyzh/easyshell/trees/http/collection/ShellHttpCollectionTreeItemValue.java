package cn.oyzh.easyshell.trees.http.collection;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.domain.http.ShellHttpCollection;
import cn.oyzh.fx.gui.svg.glyph.file.FolderSVGGlyph;
import cn.oyzh.fx.gui.tree.view.RichTreeItemValue;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.i18n.I18nHelper;
import javafx.scene.paint.Color;

/**
 * HTTP 请求集合节点值
 *
 * @author oyzh
 * @since 2026-11-08
 */
public class ShellHttpCollectionTreeItemValue extends RichTreeItemValue {

    /**
     * 构造集合节点值
     *
     * @param item 集合节点
     */
    public ShellHttpCollectionTreeItemValue(ShellHttpCollectionTreeItem item) {
        super(item);
        this.setRichMode(true);
    }

    @Override
    public ShellHttpCollectionTreeItem item() {
        return (ShellHttpCollectionTreeItem) super.item();
    }

    @Override
    public String name() {
        ShellHttpCollection collection = this.item().collection();
        return StringUtil.blankToDefault(collection.getName(), I18nHelper.collection());
    }

    @Override
    public String extra() {
        return "(" + this.item().collection().getRequests().size() + ")";
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
}
