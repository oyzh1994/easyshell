package cn.oyzh.easyshell.tabs.sqlite.table;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.sqlite.table.SQLiteTable;
import cn.oyzh.easyshell.sqlite.ShellSqliteClient;
import cn.oyzh.easyshell.tabs.sqlite.ShellSqliteBaseTab;
import cn.oyzh.fx.gui.svg.glyph.EditSVGGlyph;
import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.window.StageManager;
import cn.oyzh.i18n.I18nHelper;
import javafx.event.Event;
import javafx.scene.Cursor;

/**
 * SQLite 表设计器标签页。
 *
 * @since 2026-11-02
 */
public class ShellSqliteTableDesignTab extends ShellSqliteBaseTab {

    private ShellSqliteClient client;

    @Override
    protected String url() {
        return FXConst.TAB_PATH + "sqlite/table/shellSqliteTableDesignTab.fxml";
    }

    @Override
    public ShellSqliteClient client() {
        return this.client;
    }

    @Override
    public void flushGraphic() {
        if (this.getGraphic() == null) {
            EditSVGGlyph graphic = new EditSVGGlyph();
            graphic.setCursor(Cursor.DEFAULT);
            this.setGraphic(graphic);
        }
    }

    @Override
    public void flushTitle() {
        String name = this.tableName();
        String connectName = this.client == null ? "" : this.client.getShellConnect().getName();
        String title = StringUtil.blankToDefault(name, I18nHelper.unnamedTable()) + "@main(" + connectName + ")";
        this.setText((this.isUnsaved() ? "* " : "") + title);
    }

    public void init(ShellSqliteClient client, SQLiteTable table) {
        this.client = client;
        StageManager.showMask(this.getWindow(), () -> {
            try {
                this.controller().init(client, table);
                this.flush();
            } catch (Exception ex) {
                MessageBox.exception(ex, this.getWindow());
            }
        });
    }

    public String tableName() {
        return this.controller().tableName();
    }

    public boolean isUnsaved() {
        return this.controller().isUnsaved();
    }

    @Override
    protected void onTabCloseRequest(Event event) {
        if (this.isUnsaved() && !MessageBox.confirm(I18nHelper.unsavedAndContinue(), this.getWindow())) {
            event.consume();
        } else {
            this.closeTab();
        }
    }

    @Override
    public ShellSqliteTableDesignTabController controller() {
        return (ShellSqliteTableDesignTabController) super.controller();
    }
}
