package cn.oyzh.easyshell.popups.mariadb;

import cn.oyzh.easyshell.mariadb.column.MariadbColumn;
import cn.oyzh.easyshell.mariadb.record.MariadbRecordFilter;
import cn.oyzh.easyshell.trees.mariadb.table.ShellMariadbTableTreeItem;
import cn.oyzh.easyshell.trees.mariadb.view.ShellMariadbViewTreeItem;
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
 * @since 2026-10-09
 */
@PopupAttribute(
        value = FXConst.POPUP_PATH + "mariadb/shellMariadbRecordFilterPopup.fxml"
)
public class ShellMariadbRecordFilterPopupController extends PopupController {

    /**
     * 表过滤条件表单
     */
    @FXML
    private FXTableView<MariadbRecordFilter> filterTable;

    /**
     * db表节点
     */
    private TreeItem<?> treeItem;

    /**
     * 字段列表
     */
    private List<MariadbColumn> columnList;

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
        List<MariadbRecordFilter> filters = this.getProp("filters");
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
        MariadbRecordFilter filter = new MariadbRecordFilter();
        if (this.columnList == null) {
            if (this.treeItem instanceof ShellMariadbTableTreeItem item) {
                this.columnList = item.columns();
            } else if (this.treeItem instanceof ShellMariadbViewTreeItem item) {
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
            MariadbRecordFilter filter = this.filterTable.getSelectedItem();
            if (filter != null) {
                this.filterTable.getItems().remove(filter);
            }
        } catch (Exception ex) {
            MessageBox.exception(ex);
        }
    }
}
