package cn.oyzh.easyshell.trees.dameng.sequence;

import cn.oyzh.fx.gui.svg.glyph.database.SequenceSVGGlyph;
import cn.oyzh.fx.gui.tree.view.RichTreeItemValue;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.i18n.I18nHelper;
import javafx.scene.paint.Color;

/**
 * 达梦数据库树序列类型节点值
 *
 * @author oyzh
 * @since 2025-11-06
 */
public class ShellDamengSequencesTreeItemValue extends RichTreeItemValue {

    /**
     * 构造达梦数据库树序列类型节点值
     *
     * @param item 序列类型节点
     */
    public ShellDamengSequencesTreeItemValue(ShellDamengSequencesTreeItem item) {
        super(item);
    }

    @Override
    public ShellDamengSequencesTreeItem item() {
        return (ShellDamengSequencesTreeItem) super.item();
    }

    @Override
    public String name() {
        return I18nHelper.sequence();
    }

    @Override
    public SVGGlyph graphic() {
        if (super.graphic() == null) {
            super.graphic(new SequenceSVGGlyph());
            super.graphic().disableTheme();
        }
        return super.graphic();
    }

    @Override
    public Color graphicColor() {
        if (!this.item().isChildEmpty()) {
            return Color.GREEN;
        }
        return super.graphicColor();
    }

    @Override
    public String extra() {
        Integer size = this.item().sequenceSize();
        if (size != null) {
            return " (" + size + ")";
        }
        return super.extra();
    }

    @Override
    public Color extraColor() {
        return Color.valueOf("#2EA62E");
    }
}
