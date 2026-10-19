package cn.oyzh.easyshell.trees.mariadb.event;

import cn.oyzh.fx.gui.svg.glyph.database.EventSVGGlyph;
import cn.oyzh.fx.gui.tree.view.RichTreeItemValue;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;

/**
 * MariaDB事件节点值
 *
 * @author oyzh
 * @since 2026-10-09
 */
public class ShellMariadbEventTreeItemValue extends RichTreeItemValue {

    /**
     * 构造事件节点值
     *
     * @param item 事件节点
     */
    public ShellMariadbEventTreeItemValue(ShellMariadbEventTreeItem item) {
        super(item);
        this.setRichMode(true);
    }

    @Override
    public ShellMariadbEventTreeItem item() {
        return (ShellMariadbEventTreeItem) super.item();
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
