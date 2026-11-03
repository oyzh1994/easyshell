package cn.oyzh.easyshell.tabs.sqlite.function;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.sqlite.function.SQLiteFunction;
import cn.oyzh.easyshell.tabs.sqlite.ShellSqliteBaseTab;
import cn.oyzh.easyshell.trees.sqlite.root.ShellSqliteRootTreeItem;
import cn.oyzh.fx.gui.svg.glyph.database.FunctionSVGGlyph;
import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.i18n.I18nHelper;
import javafx.event.Event;
import javafx.scene.Cursor;

/**
 * SQLite 函数设计标签页
 *
 * @author oyzh
 * @since 2026-11-02
 */
public class ShellSqliteFunctionDesignTab extends ShellSqliteBaseTab {

    @Override
    protected String url() {
        return FXConst.TAB_PATH + "sqlite/function/shellSqliteFunctionDesignTab.fxml";
    }

    @Override
    public ShellSqliteRootTreeItem dbItem() {
        return this.controller().getDbItem();
    }

    @Override
    public void flushGraphic() {
        FunctionSVGGlyph graphic = (FunctionSVGGlyph) this.getGraphic();
        if (graphic == null) {
            graphic = new FunctionSVGGlyph();
            graphic.setCursor(Cursor.DEFAULT);
            this.setGraphic(graphic);
        }
    }

    @Override
    public void flushTitle() {
        String name = this.functionName();
        if (StringUtil.isBlank(name)) {
            name = I18nHelper.unnamedFunction();
        }
        if (this.isUnsaved()) {
            this.setText("* " + name + "@" + this.dbName() + "(" + this.connectName() + ")");
        } else {
            this.setText(name + "@" + this.dbName() + "(" + this.connectName() + ")");
        }
    }

    /**
     * 获取函数名称
     *
     * @return 函数名称
     */
    public String functionName() {
        return this.controller().functionName();
    }

    /**
     * 初始化
     *
     * @param function 函数对象
     * @param dbItem   数据库树节点
     */
    public void init(SQLiteFunction function, ShellSqliteRootTreeItem dbItem) {
        this.controller().init(function, dbItem);
        this.flush();
    }

    /**
     * 是否未保存
     *
     * @return 是否未保存
     */
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
