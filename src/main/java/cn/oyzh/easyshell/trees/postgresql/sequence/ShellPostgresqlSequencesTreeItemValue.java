package cn.oyzh.easyshell.trees.postgresql.sequence;

import cn.oyzh.fx.gui.svg.glyph.database.SequenceSVGGlyph;
import cn.oyzh.fx.gui.tree.view.RichTreeItemValue;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.i18n.I18nHelper;
import javafx.scene.paint.Color;

/**
 * PostgreSQL数据库树序列类型节点值
 *
 * @author oyzh
 * @since 2025-11-06
 */
public class ShellPostgresqlSequencesTreeItemValue extends RichTreeItemValue {

    /**
     * 构造PostgreSQL数据库树序列类型节点值
     *
     * @param item 序列类型节点
     */
    public ShellPostgresqlSequencesTreeItemValue(ShellPostgresqlSequencesTreeItem item) {
        super(item);
    }

    @Override
    public ShellPostgresqlSequencesTreeItem item() {
        return (ShellPostgresqlSequencesTreeItem) super.item();
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
