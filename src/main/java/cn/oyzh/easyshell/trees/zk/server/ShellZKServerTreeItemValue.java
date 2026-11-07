package cn.oyzh.easyshell.trees.zk.server;

import cn.oyzh.fx.gui.svg.glyph.ServerSVGGlyph;
import cn.oyzh.fx.gui.tree.view.RichTreeItemValue;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.i18n.I18nHelper;

/**
 * zk服务节点值
 *
 * @author oyzh
 * @since 2026-11-07
 */
public class ShellZKServerTreeItemValue extends RichTreeItemValue {

    /**
     * 构造服务节点值
     *
     * @param item 服务节点
     */
    public ShellZKServerTreeItemValue(ShellZKServerTreeItem item) {
        super(item);
        this.setRichMode(true);
    }

    @Override
    public ShellZKServerTreeItem item() {
        return (ShellZKServerTreeItem) super.item();
    }

    @Override
    public String name() {
        return I18nHelper.server();
    }

    @Override
    public SVGGlyph graphic() {
        if (super.graphic() == null) {
            super.graphic(new ServerSVGGlyph());
            super.graphic().disableTheme();
        }
        return super.graphic();
    }
}
