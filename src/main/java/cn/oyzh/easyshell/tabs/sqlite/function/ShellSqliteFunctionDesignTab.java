package cn.oyzh.easyshell.tabs.sqlite.function;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.sqlite.function.SQLiteFunction;
import cn.oyzh.easyshell.sqlite.ShellSqliteClient;
import cn.oyzh.easyshell.tabs.sqlite.ShellSqliteBaseTab;
import cn.oyzh.fx.gui.svg.glyph.database.FunctionSVGGlyph;
import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.window.StageManager;
import cn.oyzh.i18n.I18nHelper;
import javafx.event.Event;
import javafx.scene.Cursor;

/**
 * SQLite 函数设计器标签页。
 *
 * @since 2026-11-02
 */
public class ShellSqliteFunctionDesignTab extends ShellSqliteBaseTab {

    private ShellSqliteClient client;

    @Override
    protected String url() {
        return FXConst.TAB_PATH + "sqlite/function/shellSqliteFunctionDesignTab.fxml";
    }

    @Override
    public ShellSqliteClient client() {
        return this.client;
    }

    @Override
    public void flushGraphic() {
        if (this.getGraphic() == null) {
            FunctionSVGGlyph graphic = new FunctionSVGGlyph();
            graphic.setCursor(Cursor.DEFAULT);
            this.setGraphic(graphic);
        }
    }

    @Override
    public void flushTitle() {
        this.setText((this.isUnsaved() ? "* " : "")
                + StringUtil.blankToDefault(this.functionName(), I18nHelper.unnamedFunction()));
    }

    public void init(ShellSqliteClient client, SQLiteFunction function) {
        this.client = client;
        StageManager.showMask(this.getWindow(), () -> {
            try {
                this.controller().init(client, function);
                this.flush();
            } catch (Exception ex) {
                MessageBox.exception(ex, this.getWindow());
            }
        });
    }

    public String functionName() {
        return this.controller().functionName();
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
    public ShellSqliteFunctionDesignTabController controller() {
        return (ShellSqliteFunctionDesignTabController) super.controller();
    }
}
