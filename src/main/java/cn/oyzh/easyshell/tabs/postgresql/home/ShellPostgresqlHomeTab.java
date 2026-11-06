package cn.oyzh.easyshell.tabs.postgresql.home;

import cn.oyzh.fx.gui.svg.glyph.database.PostgreSQLSVGGlyph;
import cn.oyzh.fx.gui.tabs.RichTab;
import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.i18n.I18nHelper;
import javafx.scene.Cursor;

/**
 * postgresql主页tab
 *
 * @author oyzh
 * @since 2025-11-10
 */
public class ShellPostgresqlHomeTab extends RichTab {

    @Override
    protected String url() {
        return FXConst.TAB_PATH + "postgresql/home/shellPostgresqlHomeTab.fxml";
    }

    @Override
    public String getTabTitle() {
        return I18nHelper.info();
    }

    @Override
    public void flushGraphic() {
        PostgreSQLSVGGlyph graphic = (PostgreSQLSVGGlyph) this.getGraphic();
        if (graphic == null) {
            graphic = new PostgreSQLSVGGlyph();
            graphic.setCursor(Cursor.DEFAULT);
            this.setGraphic(graphic);
        }
    }

    @Override
    public ShellPostgresqlHomeTabController controller() {
        return (ShellPostgresqlHomeTabController) super.controller();
    }
}
