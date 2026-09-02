package cn.oyzh.easyshell.trees.mongo.user;

import cn.oyzh.fx.gui.svg.glyph.UserSVGGlyph;
import cn.oyzh.fx.gui.tree.view.RichTreeItemValue;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * db树表节点值
 *
 * @author oyzh
 * @since 2023/12/22
 */
public class ShellMongoUserTreeItemValue extends RichTreeItemValue {

    public ShellMongoUserTreeItemValue(ShellMongoUserTreeItem item) {
        super(item);
        super.setRichMode(true);
    }

    @Override
    public ShellMongoUserTreeItem item() {
        return (ShellMongoUserTreeItem) super.item();
    }

    @Override
    public SVGGlyph graphic() {
        if (super.graphic() == null) {
            super.graphic(new UserSVGGlyph());
        }
        return super.graphic();
    }

    @Override
    public String name() {
        return this.item().userName();
    }
}
