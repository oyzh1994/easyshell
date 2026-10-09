package cn.oyzh.easyshell.tabs.mariadb.home;

import cn.oyzh.fx.gui.svg.glyph.database.MariadbSVGGlyph;
import cn.oyzh.fx.gui.tabs.RichTab;
import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.i18n.I18nHelper;
import javafx.scene.Cursor;

/**
 * MariaDB 主页标签页
 *
 * @author oyzh
 * @since 2026-10-09
 */
public class ShellMariadbHomeTab extends RichTab {

    @Override
    protected String url() {
        return FXConst.TAB_PATH + "mariadb/home/shellMariadbHomeTab.fxml";
    }

    @Override
    public String getTabTitle() {
        return I18nHelper.info();
    }

    @Override
    public void flushGraphic() {
        MariadbSVGGlyph graphic = (MariadbSVGGlyph) this.getGraphic();
        if (graphic == null) {
            graphic = new MariadbSVGGlyph();
            graphic.setCursor(Cursor.DEFAULT);
            this.setGraphic(graphic);
        }
    }

    @Override
    public ShellMariadbHomeTabController controller() {
        return (ShellMariadbHomeTabController) super.controller();
    }
}
