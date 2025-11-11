package cn.oyzh.easyshell.popups.mysql;

import cn.oyzh.easyshell.mysql.column.MysqlColumn;
import cn.oyzh.easyshell.mysql.record.MysqlRecordFilter;
import cn.oyzh.easyshell.trees.mysql.table.MysqlTableTreeItem;
import cn.oyzh.easyshell.trees.mysql.view.MysqlViewTreeItem;
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
 * @since 2024/06/26
 */
@PopupAttribute(
        value = FXConst.POPUP_PATH + "mysql/shellMysqlTableRecordFilterPopup.fxml"
)
public class ShellMysqlTableRecordFilterPopupController extends PopupController {

    /**
     * 表过滤条件表单
     */
    @FXML
    private FXTableView<MysqlRecordFilter> filterTable;

    // /**
    //  * 字段列
    //  */
    // @FXML
    // private FXTableColumn<MysqlRecordFilter, ShellMysqlConditionComboBox> column;
    //
    // /**
    //  * 条件列
    //  */
    // @FXML
    // private FXTableColumn<MysqlRecordFilter, ShellMysqlConditionComboBox> condition;
    //
    // /**
    //  * 值列
    //  */
    // @FXML
    // private FXTableColumn<MysqlRecordFilter, Node> value;
    //
    // /**
    //  * 启用列
    //  */
    // @FXML
    // private FXTableColumn<MysqlRecordFilter, CheckBox> enabled;
    //
    // /**
    //  * 连接符列
    //  */
    // @FXML
    // private FXTableColumn<MysqlRecordFilter, ComboBox<String>> joinSymbol;

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
            // MysqlEventUtil.tableFiltered(this.treeItem, this.filterTable.getItems());
            // this.setProp("filters", this.filterTable.getItems());
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

    // /**
    //  * 初始化列表控件
    //  */
    // private void initTable() {
    //     this.value.setCellValueFactory(new PropertyValueFactory<>("valueControl"));
    //     this.column.setCellValueFactory(new PropertyValueFactory<>("columnControl"));
    //     this.enabled.setCellValueFactory(new PropertyValueFactory<>("enabledControl"));
    //     this.condition.setCellValueFactory(new PropertyValueFactory<>("conditionControl"));
    //     this.joinSymbol.setCellValueFactory(new PropertyValueFactory<>("joinSymbolControl"));
    // }

    // @Override
    // public void onPopupInitialize(PopupAdapter window) {
    //     super.onPopupInitialize(window);
    //     // 初始化表单
    //     this.initTable();
    // }

    /**
     * 添加过滤条件
     */
    @FXML
    private void addFilter() {
        MysqlRecordFilter filter = new MysqlRecordFilter();
        if (this.columnList == null) {
            if (this.treeItem instanceof MysqlTableTreeItem item) {
                this.columnList = item.columns();
            } else if (this.treeItem instanceof MysqlViewTreeItem item) {
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
