package cn.oyzh.easyshell.tabs.sqlite.view;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.sqlite.view.SQLiteView;
import cn.oyzh.easyshell.tabs.sqlite.ShellSqliteBaseTab;
import cn.oyzh.easyshell.trees.sqlite.root.ShellSqliteRootTreeItem;
import cn.oyzh.fx.gui.svg.glyph.database.ViewSVGGlyph;
import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.window.StageManager;
import cn.oyzh.i18n.I18nHelper;
import javafx.event.Event;
import javafx.scene.Cursor;

/**
 * SQLite 视图设计标签页
 *
 * @author oyzh
 * @since 2026-11-02
 */
public class ShellSqliteViewDesignTab extends ShellSqliteBaseTab {

    @Override
    protected String url() {
        return FXConst.TAB_PATH + "sqlite/view/shellSqliteViewDesignTab.fxml";
    }

    @Override
    public void flushGraphic() {
        SVGGlyph graphic = (SVGGlyph) this.getGraphic();
        if (graphic == null) {
            graphic = new ViewSVGGlyph();
            graphic.setCursor(Cursor.DEFAULT);
            this.setGraphic(graphic);
        }
    }

    @Override
    public void flushTitle() {
        String name = this.viewName();
        if (StringUtil.isBlank(name)) {
            name = I18nHelper.unnamedView();
        }
        if (this.isUnsaved()) {
            this.setText("* " + name + "@" + this.dbName() + "(" + this.connectName() + ")");
        } else {
            this.setText(name + "@" + this.dbName() + "(" + this.connectName() + ")");
        }
    }

    /**
     * 获取视图名称
     *
     * @return 视图名称
     */
    public String viewName() {
        return this.controller().viewName();
    }

    @Override
    public ShellSqliteRootTreeItem dbItem() {
        return this.controller().getDbItem();
    }

    /**
     * 初始化
     *
     * @param view   视图对象
     * @param dbItem 数据库树节点
     */
    public void init(SQLiteView view, ShellSqliteRootTreeItem dbItem) {
        this.controller().init(view, dbItem);
        this.flush();
    }

    @Override
    public ShellSqliteViewDesignTabController controller() {
        return (ShellSqliteViewDesignTabController) super.controller();
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
}
