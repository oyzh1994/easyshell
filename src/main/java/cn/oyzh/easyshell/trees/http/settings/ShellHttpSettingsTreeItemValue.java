package cn.oyzh.easyshell.trees.http.settings;

import cn.oyzh.fx.gui.svg.glyph.SettingSVGGlyph;
import cn.oyzh.fx.gui.tree.view.RichTreeItemValue;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.i18n.I18nHelper;

/**
 * HTTP 设置节点值
 *
 * @author oyzh
 * @since 2026-11-08
 */
public class ShellHttpSettingsTreeItemValue extends RichTreeItemValue {

    /**
     * 构造设置节点值
     *
     * @param item 设置节点
     */
    public ShellHttpSettingsTreeItemValue(ShellHttpSettingsTreeItem item) {
        super(item);
        this.setRichMode(true);
    }

    @Override
    public ShellHttpSettingsTreeItem item() {
        return (ShellHttpSettingsTreeItem) super.item();
    }

    @Override
    public String name() {
        return I18nHelper.setting();
    }

    @Override
    public SVGGlyph graphic() {
        if (super.graphic() == null) {
            super.graphic(new SettingSVGGlyph());
            super.graphic().disableTheme();
        }
        return super.graphic();
    }
}
