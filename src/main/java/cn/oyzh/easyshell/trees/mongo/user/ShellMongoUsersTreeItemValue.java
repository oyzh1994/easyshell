package cn.oyzh.easyshell.trees.mongo.user;

import cn.oyzh.fx.gui.svg.glyph.UserSVGGlyph;
import cn.oyzh.fx.gui.tree.view.RichTreeItemValue;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.i18n.I18nHelper;
import javafx.scene.paint.Color;

/**
 * db树表类型值
 *
 * @author oyzh
 * @since 2023/12/08
 */
public class ShellMongoUsersTreeItemValue extends RichTreeItemValue {

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
