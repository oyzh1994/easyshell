package cn.oyzh.easyshell.trees.mysql.event;

import cn.oyzh.fx.gui.svg.glyph.database.EventSVGGlyph;
import cn.oyzh.fx.gui.tree.view.RichTreeItemValue;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.i18n.I18nHelper;
import javafx.scene.paint.Color;

/**
 * @author oyzh
 * @since 2024/09/09
 */
public class ShellMysqlEventsTreeItemValue extends RichTreeItemValue {

    public ShellMysqlEventsTreeItemValue(ShellMysqlEventsTreeItem item) {
        super(item);
    }

    @Override
    protected ShellMysqlEventsTreeItem item() {
        return (ShellMysqlEventsTreeItem) super.item();
    }

    @Override
    public String name() {
        return I18nHelper.event();
    }

    @Override
    public SVGGlyph graphic() {
        if (this.graphic == null) {
            this.graphic = new EventSVGGlyph("12");
            this.graphic.disableTheme();
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
        Integer size = this.item().getEventSize();
        if (size != null) {
            return " (" + size + ")";
        }
        return super.extra();
    }

    @Override
    public Color extraColor() {
        return Color.valueOf("#228B22");
    }
}
