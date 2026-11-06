package cn.oyzh.easyshell.popups.postgresql;

import cn.oyzh.easyshell.postgresql.column.PostgresqlColumn;
import cn.oyzh.easyshell.postgresql.record.PostgresqlRecordFilter;
import cn.oyzh.easyshell.tabs.postgresql.ShellPostgresqlTab;
import cn.oyzh.easyshell.trees.postgresql.ShellPostgresqlTreeItem;
import cn.oyzh.easyshell.trees.postgresql.table.ShellPostgresqlTableTreeItem;
import cn.oyzh.easyshell.trees.postgresql.view.ShellPostgresqlViewTreeItem;
import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.fx.plus.controller.PopupController;
import cn.oyzh.fx.plus.controls.table.FXTableView;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.window.PopupAttribute;
import javafx.fxml.FXML;
import javafx.scene.control.TreeItem;
import javafx.stage.WindowEvent;

import java.util.List;

import static atlantafx.base.controls.Popover.ArrowLocation.BOTTOM_LEFT;
import static javafx.stage.PopupWindow.AnchorLocation.CONTENT_TOP_LEFT;

/**
 * 数据过滤业务
 *
 * @author oyzh
 * @since 2025-11-06
 */
@PopupAttribute(
        value = FXConst.POPUP_PATH + "postgresql/shellPostgresqlRecordFilterPopup.fxml"
)
public class ShellPostgresqlRecordFilterPopupController extends PopupController {

    /**
     * 表过滤条件表单
     */
    @FXML
    private FXTableView<PostgresqlRecordFilter> filterTable;

    /**
     * db表节点
     */
    private TreeItem<?> treeItem;

    /**
     * 字段列表
     */
    private List<PostgresqlColumn> columnList;

    /**
     * 应用
     */
    @FXML
    private void apply() {
        try {
            this.submit(this.filterTable.getItems());
            this.closeWindow();
        } catch (Exception ex) {
            MessageBox.exception(ex, this.getWindow());
        }
    }

    /**
     * 关闭
     */
    @FXML
    private void close() {
        this.closeWindow();
    }

    @Override
    protected void bindListeners() {
        super.bindListeners();
    }

    @Override
    public void onWindowShowing(WindowEvent event) {
        super.onWindowShowing(event);
        this.treeItem = this.getProp("item");
        List<PostgresqlRecordFilter> filters = this.getProp("filters");
        this.filterTable.setItem(filters);
    }

    @Override
    public void onWindowHidden(WindowEvent event) {
        super.onWindowHidden(event);
        this.columnList = null;
    }

    /**
     * 添加过滤条件
     */
    @FXML
    private void addFilter() {
        PostgresqlRecordFilter filter = new PostgresqlRecordFilter();
        if (this.columnList == null) {
            if (this.treeItem instanceof ShellPostgresqlTableTreeItem item) {
                this.columnList = item.columns();
            } else if (this.treeItem instanceof ShellPostgresqlViewTreeItem item) {
                this.columnList = item.columns();
            }
        }
        filter.setColumns(this.columnList);
        this.filterTable.addItem(filter);
    }

    /**
     * 删除过滤条件
     */
    @FXML
    private void deleteFilter() {
        try {
            PostgresqlRecordFilter filter = this.filterTable.getSelectedItem();
            if (filter != null) {
                this.filterTable.getItems().remove(filter);
            }
        } catch (Exception ex) {
            MessageBox.exception(ex, this.getWindow());
        }
    }
}
