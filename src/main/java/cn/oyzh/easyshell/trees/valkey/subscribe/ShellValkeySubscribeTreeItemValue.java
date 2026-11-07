package cn.oyzh.easyshell.trees.valkey.subscribe;

import cn.oyzh.easyshell.fx.svg.glyph.SubscribeSVGGlyph;
import cn.oyzh.fx.gui.tree.view.RichTreeItemValue;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.i18n.I18nHelper;
import cn.oyzh.i18n.I18nResourceBundle;

/**
 * redis订阅节点值
 *
 * @author oyzh
 * @since 2026-11-07
 */
public class ShellValkeySubscribeTreeItemValue extends RichTreeItemValue {

    /**
     * 构造订阅节点值
     *
     * @param item 订阅节点
     */
    public ShellValkeySubscribeTreeItemValue(ShellValkeySubscribeTreeItem item) {
        super(item);
        this.setRichMode(true);
    }

    @Override
    public ShellValkeySubscribeTreeItem item() {
        return (ShellValkeySubscribeTreeItem) super.item();
    }

    @Override
    public String name() {
        return I18nHelper.subscribe() + "/" + I18nResourceBundle.i18nString("base.publish");
    }

    @Override
    public SVGGlyph graphic() {
        if (super.graphic() == null) {
            super.graphic(new SubscribeSVGGlyph());
            super.graphic().disableTheme();
        }
        return super.graphic();
    }
}
