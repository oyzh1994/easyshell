package cn.oyzh.easyshell.tabs.h2.home;

import cn.oyzh.fx.gui.svg.glyph.database.H2SVGGlyph;
import cn.oyzh.fx.gui.tabs.RichTab;
import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.i18n.I18nHelper;
import javafx.scene.Cursor;

/**
 * H2 主页标签页
 *
 * @author oyzh
 * @since 2026-11-02
 */
public class ShellH2HomeTab extends RichTab {

    @Override
    protected String url() {
        return FXConst.TAB_PATH + "h2/home/shellH2HomeTab.fxml";
    }

    @Override
    public ShellH2HomeTabController controller() {
        return (ShellH2HomeTabController) super.controller();
    }

    @Override
    public String getTabTitle() {
        return I18nHelper.info();
    }

    @Override
    public void flushGraphic() {
        SVGGlyph graphic = (SVGGlyph) this.getGraphic();
        if (graphic == null) {
            graphic = new H2SVGGlyph();
            graphic.setCursor(Cursor.DEFAULT);
            this.setGraphic(graphic);
        }
    }

}
