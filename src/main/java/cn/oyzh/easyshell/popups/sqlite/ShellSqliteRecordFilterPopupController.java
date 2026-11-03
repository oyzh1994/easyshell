package cn.oyzh.easyshell.popups.sqlite;

import cn.oyzh.easyshell.sqlite.column.SQLiteColumn;
import cn.oyzh.easyshell.sqlite.record.SQLiteRecordFilter;
import cn.oyzh.easyshell.trees.sqlite.table.ShellSqliteTableTreeItem;
import cn.oyzh.easyshell.trees.sqlite.view.ShellSqliteViewTreeItem;
import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.fx.plus.controller.PopupController;
import cn.oyzh.fx.plus.controls.table.FXTableView;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.window.PopupAttribute;
import javafx.fxml.FXML;
import javafx.scene.control.TreeItem;
import javafx.stage.WindowEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * SQLite 记录过滤条件弹窗。
 *
 * @since 2026-11-02
 */
@PopupAttribute(
        value = FXConst.POPUP_PATH + "sqlite/shellSqliteRecordFilterPopup.fxml"
)
public class ShellSqliteRecordFilterPopupController extends PopupController {

    @FXML
    private FXTableView<SQLiteRecordFilter> filterTable;

    /**
     * 数据库表或视图节点
     */
    private TreeItem<?> treeItem;

    /**
     * 字段列表
     */
    private List<SQLiteColumn> columnList;

    @FXML
    private void apply() {
        try {
            this.submit(this.filterTable.getItems());
            this.closeWindow();
        } catch (Exception ex) {
            MessageBox.exception(ex, this.getWindow());
        }
    }

    @FXML
    private void close() {
        this.closeWindow();
    }

    @Override
    public void onWindowShowing(WindowEvent event) {
        super.onWindowShowing(event);
        this.treeItem = this.getProp("item");
        List<SQLiteRecordFilter> filters = this.getProp("filters");
        this.filterTable.setItem(filters == null ? new ArrayList<>() : filters);
    }

    @Override
    public void onWindowHidden(WindowEvent event) {
        super.onWindowHidden(event);
        this.columnList = null;
    }

    @FXML
    private void addFilter() {
        SQLiteRecordFilter filter = new SQLiteRecordFilter();
        if (this.columnList == null) {
            if (this.treeItem instanceof ShellSqliteTableTreeItem item) {
                this.columnList = item.columns();
            } else if (this.treeItem instanceof ShellSqliteViewTreeItem item) {
                this.columnList = item.columns();
            }
        }
        filter.setColumns(this.columnList);
        this.filterTable.addItem(filter);
    }

    @FXML
    private void deleteFilter() {
        SQLiteRecordFilter filter = this.filterTable.getSelectedItem();
        if (filter != null) {
            this.filterTable.getItems().remove(filter);
        }
    }
}
