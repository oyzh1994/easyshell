package cn.oyzh.easyshell.tabs.sqlite.home;

import cn.oyzh.easyshell.sqlite.ShellSqliteClient;
import cn.oyzh.easyshell.tabs.sqlite.ShellSqliteBaseTab;
import cn.oyzh.fx.gui.svg.glyph.database.SqliteSVGGlyph;
import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.i18n.I18nHelper;
import javafx.scene.Cursor;

/**
 * SQLite 主页标签。
 *
 * @since 2026-11-02
 */
public class ShellSqliteHomeTab extends ShellSqliteBaseTab {

    private ShellSqliteClient client;

    @Override
    protected String url() {
        return FXConst.TAB_PATH + "sqlite/home/shellSqliteHomeTab.fxml";
    }

    @Override
    public ShellSqliteClient client() {
        return this.client;
    }

    public void init(ShellSqliteClient client) {
        this.client = client;
        this.controller().init(client);
    }

    protected ShellSqliteHomeTabController controller() {
        return (ShellSqliteHomeTabController) super.controller();
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

    @Override
    public void flushTitle() {
        this.setText(I18nHelper.database());
    }
}
