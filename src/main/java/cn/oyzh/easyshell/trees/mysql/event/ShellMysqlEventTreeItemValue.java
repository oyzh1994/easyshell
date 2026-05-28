package cn.oyzh.easyshell.trees.mysql.event;

import cn.oyzh.fx.gui.svg.glyph.database.EventSVGGlyph;
import cn.oyzh.fx.gui.tree.view.RichTreeItemValue;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * @author oyzh
 * @since 2024/09/09
 */
public class ShellMysqlEventTreeItemValue extends RichTreeItemValue {

    public ShellMysqlEventTreeItemValue(ShellMysqlEventTreeItem item) {
        super(item);
        this.setRichMode(true);
    }

    @Override
    public ShellMysqlEventTreeItem item() {
        return (ShellMysqlEventTreeItem) super.item();
    }

    @Override
    public SVGGlyph graphic() {
        if (super.graphic() == null) {
            super.graphic(new EventSVGGlyph());
        }
        return super.graphic();
    }

    @Override
    public String name() {
        return this.item().eventName();
    }
}
