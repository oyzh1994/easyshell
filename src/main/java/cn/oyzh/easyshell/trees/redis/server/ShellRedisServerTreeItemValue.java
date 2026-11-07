package cn.oyzh.easyshell.trees.redis.server;

import cn.oyzh.fx.gui.svg.glyph.ServerSVGGlyph;
import cn.oyzh.fx.gui.tree.view.RichTreeItemValue;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.i18n.I18nHelper;

/**
 * redis服务器信息节点值
 *
 * @author oyzh
 * @since 2026-11-07
 */
public class ShellRedisServerTreeItemValue extends RichTreeItemValue {

    /**
     * 构造服务器信息节点值
     *
     * @param item 服务器信息节点
     */
    public ShellRedisServerTreeItemValue(ShellRedisServerTreeItem item) {
        super(item);
        this.setRichMode(true);
    }

    @Override
    public ShellRedisServerTreeItem item() {
        return (ShellRedisServerTreeItem) super.item();
    }

    @Override
    public String name() {
        return I18nHelper.serverInfo();
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
