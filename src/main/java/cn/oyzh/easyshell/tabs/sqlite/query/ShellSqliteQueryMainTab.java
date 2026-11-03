package cn.oyzh.easyshell.tabs.sqlite.query;

import cn.oyzh.easyshell.domain.ShellQuery;
import cn.oyzh.easyshell.sqlite.ShellSqliteClient;
import cn.oyzh.easyshell.tabs.sqlite.ShellSqliteBaseTab;
import cn.oyzh.fx.gui.svg.glyph.QuerySVGGlyph;
import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.i18n.I18nHelper;
import javafx.event.Event;
import javafx.scene.Cursor;

/**
 * SQLite 查询标签。
 *
 * @since 2026-11-02
 */
public class ShellSqliteQueryMainTab extends ShellSqliteBaseTab {

    private ShellSqliteClient client;

    @Override
    protected String url() {
        return FXConst.TAB_PATH + "sqlite/query/shellSqliteQueryMainTab.fxml";
    }

    @Override
    public ShellSqliteClient client() {
        return this.client;
    }

    public void init(ShellQuery query, ShellSqliteClient client) {
        this.client = client;
        this.controller().init(query, client);
        this.flush();
    }

    protected ShellSqliteQueryMainTabController controller() {
        return (ShellSqliteQueryMainTabController) super.controller();
    }

    public ShellQuery query() {
        return this.controller().getQuery();
    }

    public boolean isUnsaved() {
        return this.controller().isUnsaved();
    }

    @Override
    public void flushGraphic() {
        SVGGlyph graphic = (SVGGlyph) this.getGraphic();
        if (graphic == null) {
            graphic = new QuerySVGGlyph();
            graphic.setCursor(Cursor.DEFAULT);
            this.setGraphic(graphic);
        }
    }

    @Override
    public void flushTitle() {
        String name = this.query().getName();
        if (name == null) {
            name = I18nHelper.newQuery();
        }
        this.setText((this.isUnsaved() ? "* " : "") + name + " (SQLite)");
    }

    @Override
    protected void onTabCloseRequest(Event event) {
        if (this.isUnsaved() && !MessageBox.confirm(I18nHelper.unsavedAndContinue(), this.getWindow())) {
            event.consume();
        } else {
            this.closeTab();
        }
    }
}
