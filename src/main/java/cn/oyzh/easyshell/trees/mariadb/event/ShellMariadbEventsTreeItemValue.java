package cn.oyzh.easyshell.trees.mariadb.event;

import cn.oyzh.fx.gui.svg.glyph.database.EventSVGGlyph;
import cn.oyzh.fx.gui.tree.view.RichTreeItemValue;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.i18n.I18nHelper;
import javafx.scene.paint.Color;

/**
 * MariaDB事件类型节点值
 *
 * @author oyzh
 * @since 2026-10-09
 */
public class ShellMariadbEventsTreeItemValue extends RichTreeItemValue {

    /**
     * 构造事件类型节点值
     *
     * @param item 事件类型节点
     */
    public ShellMariadbEventsTreeItemValue(ShellMariadbEventsTreeItem item) {
        super(item);
    }

    @Override
    public ShellMariadbEventsTreeItem item() {
        return (ShellMariadbEventsTreeItem) super.item();
    }

    @Override
    public String name() {
        return I18nHelper.event();
    }

    @Override
    public SVGGlyph graphic() {
        if (super.graphic() == null) {
            super.graphic(new EventSVGGlyph());
            super.graphic().disableTheme();
        }
        return super.graphic();
    }

//    @Override
//    public Color graphicColor() {
//        if (!this.item().isChildEmpty()) {
//            return Color.GREEN;
//        }
//        return super.graphicColor();
//    }

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
