package cn.oyzh.easyshell.trees.mongo.collection;

import cn.oyzh.fx.gui.svg.glyph.database.TableSVGGlyph;
import cn.oyzh.fx.gui.tree.view.RichTreeItemValue;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.i18n.I18nHelper;
import javafx.scene.paint.Color;

/**
 * mongodb集合类型节点值
 *
 * @author oyzh
 * @since 2025-11-06
 */
public class ShellMongoCollectionsTreeItemValue extends RichTreeItemValue {

    /**
     * 构造集合类型节点值
     *
     * @param item 集合类型节点
     */
    public ShellMongoCollectionsTreeItemValue(ShellMongoCollectionsTreeItem item) {
        super(item);
    }

    @Override
    public ShellMongoCollectionsTreeItem item() {
        return (ShellMongoCollectionsTreeItem) super.item();
    }

    @Override
    public String name() {
        return I18nHelper.collections();
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
    public String extra() {
        Integer size = this.item().getCollectionsSize();
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
