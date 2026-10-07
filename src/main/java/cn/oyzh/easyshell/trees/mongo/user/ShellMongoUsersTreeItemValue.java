package cn.oyzh.easyshell.trees.mongo.user;

import cn.oyzh.fx.gui.svg.glyph.UserSVGGlyph;
import cn.oyzh.fx.gui.tree.view.RichTreeItemValue;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.i18n.I18nHelper;
import javafx.scene.paint.Color;

/**
 * mongodb用户类型节点值
 *
 * @author oyzh
 * @since 2025-11-06
 */
public class ShellMongoUsersTreeItemValue extends RichTreeItemValue {

    /**
     * 构造用户类型节点值
     *
     * @param item 用户类型节点
     */
    public ShellMongoUsersTreeItemValue(ShellMongoUsersTreeItem item) {
        super(item);
    }

    @Override
    public ShellMongoUsersTreeItem item() {
        return (ShellMongoUsersTreeItem) super.item();
    }

    @Override
    public String name() {
        return I18nHelper.users();
    }

    @Override
    public SVGGlyph graphic() {
        if (super.graphic() == null) {
            super.graphic(new UserSVGGlyph());
            super.graphic().disableTheme();
        }
        return super.graphic();
    }

    @Override
    public String extra() {
        Integer size = this.item().getUserSize();
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
