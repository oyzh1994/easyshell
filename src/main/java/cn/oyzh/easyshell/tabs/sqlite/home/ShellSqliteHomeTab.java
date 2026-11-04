package cn.oyzh.easyshell.tabs.sqlite.home;

import cn.oyzh.fx.gui.svg.glyph.database.SqliteSVGGlyph;
import cn.oyzh.fx.gui.tabs.RichTab;
import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.i18n.I18nHelper;
import javafx.scene.Cursor;

/**
 * SQLite 主页标签页
 *
 * @author oyzh
 * @since 2026-11-02
 */
public class ShellSqliteHomeTab extends RichTab {

    @Override
    protected String url() {
        return FXConst.TAB_PATH + "sqlite/home/shellSqliteHomeTab.fxml";
    }

    @Override
    public ShellSqliteHomeTabController controller() {
        return (ShellSqliteHomeTabController) super.controller();
    }

    @Override
    public String getTabTitle() {
        return I18nHelper.info();
    }

    @Override
    public void flushGraphic() {
        SVGGlyph graphic = (SVGGlyph) this.getGraphic();
        if (graphic == null) {
            graphic = new SqliteSVGGlyph();
            graphic.setCursor(Cursor.DEFAULT);
            this.setGraphic(graphic);
        }
    }

}
