package cn.oyzh.easyshell.trees.redis.root;

import cn.oyzh.easyshell.fx.svg.glyph.redis.KeysSVGGlyph;
import cn.oyzh.fx.gui.tree.view.RichTreeItemValue;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.i18n.I18nHelper;
import javafx.scene.paint.Color;

/**
 * @author oyzh
 * @since 2025-02-10
 */
public class ShellRedisKeyRootTreeItemValue extends RichTreeItemValue {

    public ShellRedisKeyRootTreeItemValue(ShellRedisKeyRootTreeItem item) {
        super(item);
        this.setRichMode(true);
    }

    @Override
    public ShellRedisKeyRootTreeItem item() {
        return (ShellRedisKeyRootTreeItem) super.item();
    }

    @Override
    public SVGGlyph graphic() {
        if (super.graphic() != null && super.graphic().isWaiting()) {
            //            super.graphic().enableTheme();
            return super.graphic();
        }
        if (super.graphic() == null) {
            super.graphic(new KeysSVGGlyph());
            super.graphic().disableTheme();
        }
        return super.graphic();
    }

    @Override
    public String name() {
        return I18nHelper.database();
    }

    @Override
    public String extra() {
        int size = this.item().keyChildrenSize();
        String extra = "(" + size + ")";
        // String filterPattern = this.item().getTreeView().getFilterPattern();
        // if (StringUtil.isNotBlank(filterPattern)) {
        //     extra += "[" + I18nHelper.keyFilter() + ":" + filterPattern + "]";
        // }
        return extra;
    }

    @Override
    public Color extraColor() {
        return Color.FORESTGREEN;
    }
}
