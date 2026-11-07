package cn.oyzh.easyshell.tabs.valkey.home;

import cn.oyzh.easyshell.fx.svg.glyph.valkey.ValkeySVGGlyph;
import cn.oyzh.fx.gui.tabs.RichTab;
import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.i18n.I18nHelper;
import javafx.scene.Cursor;

/**
 * redis主页标签页
 *
 * @author oyzh
 * @since 2026-11-07
 */
public class ShellValkeyHomeTab extends RichTab {

    @Override
    protected String url() {
        return FXConst.TAB_PATH + "valkey/home/shellValkeyHomeTab.fxml";
    }

    @Override
    public String getTabTitle() {
        return I18nHelper.info();
    }

    @Override
    public void flushGraphic() {
        SVGGlyph graphic = (SVGGlyph) this.getGraphic();
        if (graphic == null) {
            graphic = new ValkeySVGGlyph();
            graphic.setCursor(Cursor.DEFAULT);
            this.setGraphic(graphic);
        }
    }

    @Override
    public ShellValkeyHomeTabController controller() {
        return (ShellValkeyHomeTabController) super.controller();
    }
}
