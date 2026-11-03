package cn.oyzh.easyshell.tabs.sqlite.view;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.sqlite.view.SQLiteView;
import cn.oyzh.easyshell.sqlite.ShellSqliteClient;
import cn.oyzh.easyshell.tabs.sqlite.ShellSqliteBaseTab;
import cn.oyzh.fx.gui.svg.glyph.database.ViewSVGGlyph;
import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.window.StageManager;
import cn.oyzh.i18n.I18nHelper;
import javafx.event.Event;
import javafx.scene.Cursor;

/**
 * SQLite 视图设计器标签页。
 *
 * @since 2026-11-02
 */
public class ShellSqliteViewDesignTab extends ShellSqliteBaseTab {

    private ShellSqliteClient client;

    @Override
    protected String url() {
        return FXConst.TAB_PATH + "sqlite/view/shellSqliteViewDesignTab.fxml";
    }

    @Override
    public ShellSqliteClient client() {
        return this.client;
    }

    @Override
    public void flushGraphic() {
        if (this.getGraphic() == null) {
            ViewSVGGlyph graphic = new ViewSVGGlyph();
            graphic.setCursor(Cursor.DEFAULT);
            this.setGraphic(graphic);
        }
    }

    @Override
    public void flushTitle() {
        this.setText((this.isUnsaved() ? "* " : "")
                + StringUtil.blankToDefault(this.viewName(), I18nHelper.unnamedView()));
    }

    public void init(ShellSqliteClient client, SQLiteView view) {
        this.client = client;
        StageManager.showMask(this.getWindow(), () -> {
            try {
                this.controller().init(client, view);
                this.flush();
            } catch (Exception ex) {
                MessageBox.exception(ex, this.getWindow());
            }
        });
    }

    public String viewName() {
        return this.controller().viewName();
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
    public ShellSqliteViewDesignTabController controller() {
        return (ShellSqliteViewDesignTabController) super.controller();
    }
}
