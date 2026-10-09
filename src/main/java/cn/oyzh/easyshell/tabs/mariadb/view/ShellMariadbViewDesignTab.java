package cn.oyzh.easyshell.tabs.mariadb.view;

import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.mariadb.view.MariadbView;
import cn.oyzh.easyshell.tabs.mariadb.ShellMariadbBaseTab;
import cn.oyzh.easyshell.trees.mariadb.database.ShellMariadbDatabaseTreeItem;
import cn.oyzh.fx.gui.svg.glyph.EditSVGGlyph;
import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.i18n.I18nHelper;
import javafx.event.Event;
import javafx.scene.Cursor;

/**
 * MariaDB 视图设计标签页
 *
 * @author oyzh
 * @since 2026-10-09
 */
public class ShellMariadbViewDesignTab extends ShellMariadbBaseTab {

    @Override
    protected String url() {
        return FXConst.TAB_PATH + "mariadb/view/shellMariadbViewDesignTab.fxml";
    }

    @Override
    public void flushGraphic() {
        SVGGlyph graphic = (SVGGlyph) this.getGraphic();
        if (graphic == null) {
            graphic = new EditSVGGlyph();
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
    public ShellMariadbDatabaseTreeItem dbItem() {
        return this.controller().getDbItem();
    }

    /**
     * 初始化
     *
     * @param view 视图对象
     * @param item 数据库树节点
     */
    public void init(MariadbView view, ShellMariadbDatabaseTreeItem item) {
        this.controller().init(view, item);
        // 刷新tab
        this.flush();
    }

    @Override
    public ShellMariadbViewDesignTabController controller() {
        return (ShellMariadbViewDesignTabController) super.controller();
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
        if (this.isUnsaved() && !MessageBox.confirm(I18nHelper.unsavedAndContinue())) {
            event.consume();
        } else {
            this.closeTab();
        }
    }
}
