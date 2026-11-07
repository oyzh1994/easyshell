package cn.oyzh.easyshell.trees.valkey.key;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.fx.svg.glyph.valkey.KeysSVGGlyph;
import cn.oyzh.easyshell.util.ShellI18nHelper;
import cn.oyzh.fx.gui.tree.view.RichTreeItemValue;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.i18n.I18nHelper;
import javafx.scene.paint.Color;

/**
 * redis键列表节点值
 *
 * @author oyzh
 * @since 2026-11-07
 */
public class ShellValkeyKeysTreeItemValue extends RichTreeItemValue {

    /**
     * 构造键列表节点值
     *
     * @param item 键列表节点
     */
    public ShellValkeyKeysTreeItemValue(ShellValkeyKeysTreeItem item) {
        super(item);
        this.setRichMode(true);
    }

    @Override
    public ShellValkeyKeysTreeItem item() {
        return (ShellValkeyKeysTreeItem) super.item();
    }

    @Override
    public String name() {
        return I18nHelper.keys();
    }

    @Override
    public SVGGlyph graphic() {
        if (super.graphic() == null) {
            super.graphic(new KeysSVGGlyph());
            super.graphic().disableTheme();
        }
        return super.graphic();
    }

    @Override
    public String extra() {
        try {
            String extra;
            Long size = this.item().dbSize();
            if (size != null) {
                extra = "(" + size + ")";
            } else {
                extra = "(" + this.item().keyChildrenSize() + ")";
            }
            String filterPattern = this.item().getFilterPattern();
            if (StringUtil.isNotBlank(filterPattern)) {
                extra += "[" + ShellI18nHelper.label(I18nHelper.keyFilter(), filterPattern) + "]";
            }
            return extra;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return super.extra();
    }

    @Override
    public Color extraColor() {
        return Color.FORESTGREEN;
    }
}
