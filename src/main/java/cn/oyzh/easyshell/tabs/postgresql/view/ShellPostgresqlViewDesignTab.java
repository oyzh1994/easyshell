package cn.oyzh.easyshell.tabs.postgresql.view;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.postgresql.view.PostgresqlView;
import cn.oyzh.easyshell.tabs.postgresql.ShellPostgresqlBaseTab;
import cn.oyzh.easyshell.trees.postgresql.schema.ShellPostgresqlSchemaTreeItem;
import cn.oyzh.fx.gui.svg.glyph.database.ViewSVGGlyph;
import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.i18n.I18nHelper;
import javafx.event.Event;
import javafx.scene.Cursor;

/**
 * db视图设计tab
 *
 * @author oyzh
 * @since 2025-11-06
 */
public class ShellPostgresqlViewDesignTab extends ShellPostgresqlBaseTab {

    @Override
    protected String url() {
        return FXConst.TAB_PATH + "postgresql/view/shellPostgresqlViewDesignTab.fxml";
    }

    @Override
    public void flushGraphic() {
        ViewSVGGlyph graphic = (ViewSVGGlyph) this.getGraphic();
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
        // 设置提示文本
        if (this.isUnsaved()) {
            this.setText("* " + this.schema() + "-" + name);
        } else {
            this.setText(this.schema() + "-" + name);
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
    public ShellPostgresqlSchemaTreeItem dbItem() {
        return this.controller().getDbItem();
    }

    /**
     * 初始化
     *
     * @param view 视图
     * @param item 树键
     */
    public void init(PostgresqlView view, ShellPostgresqlSchemaTreeItem item) {
        this.controller().init(view, item);
        // 刷新tab
        this.flush();
    }

    @Override
    public ShellPostgresqlViewDesignTabController controller() {
        return (ShellPostgresqlViewDesignTabController) super.controller();
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
