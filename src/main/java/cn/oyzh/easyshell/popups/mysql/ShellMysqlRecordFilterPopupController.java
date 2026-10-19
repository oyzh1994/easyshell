package cn.oyzh.easyshell.popups.mysql;

import cn.oyzh.easyshell.mysql.column.MysqlColumn;
import cn.oyzh.easyshell.mysql.record.MysqlRecordFilter;
import cn.oyzh.easyshell.trees.mysql.table.ShellMysqlTableTreeItem;
import cn.oyzh.easyshell.trees.mysql.view.ShellMysqlViewTreeItem;
import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.fx.plus.controller.PopupController;
import cn.oyzh.fx.plus.controls.table.FXTableView;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.window.PopupAttribute;
import javafx.fxml.FXML;
import javafx.scene.control.TreeItem;
import javafx.stage.WindowEvent;

import java.util.List;

/**
 * 数据过滤业务
 *
 * @author oyzh
 * @since 2025-11-06
 */
@PopupAttribute(
        value = FXConst.POPUP_PATH + "mysql/shellMysqlRecordFilterPopup.fxml"
)
public class ShellMysqlRecordFilterPopupController extends PopupController {

    /**
     * 表过滤条件表单
     */
    @FXML
    private FXTableView<MysqlRecordFilter> filterTable;

    /**
     * db表节点
     */
    private TreeItem<?> treeItem;

    /**
     * 字段列表
     */
    private List<MysqlColumn> columnList;

    /**
     * 应用
     */
    @FXML
    private void apply() {
        try {
            this.submit(this.filterTable.getItems());
            this.closeWindow();
        } catch (Exception ex) {
            MessageBox.exception(ex);
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
        List<MysqlRecordFilter> filters = this.getProp("filters");
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
        MysqlRecordFilter filter = new MysqlRecordFilter();
        if (this.columnList == null) {
            if (this.treeItem instanceof ShellMysqlTableTreeItem item) {
                this.columnList = item.columns();
            } else if (this.treeItem instanceof ShellMysqlViewTreeItem item) {
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
            MysqlRecordFilter filter = this.filterTable.getSelectedItem();
            if (filter != null) {
                this.filterTable.getItems().remove(filter);
            }
        } catch (Exception ex) {
            MessageBox.exception(ex);
        }
    }
}
