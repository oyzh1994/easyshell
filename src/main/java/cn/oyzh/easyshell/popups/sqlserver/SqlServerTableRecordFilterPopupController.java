//package cn.oyzh.easyshell.popups.sqlserver;
//
//import cn.oyzh.easyshell.sqlserver.column.SqlServerColumn;
//import cn.oyzh.easyshell.sqlserver.record.SqlServerRecordFilter;
//import cn.oyzh.easyshell.trees.sqlserver.table.ShellSqlServerTableTreeItem;
//import cn.oyzh.fx.plus.FXConst;
//import cn.oyzh.fx.plus.controller.PopupController;
//import cn.oyzh.fx.plus.controls.table.FXTableView;
//import cn.oyzh.fx.plus.information.MessageBox;
//import cn.oyzh.fx.plus.window.PopupAttribute;
//import javafx.fxml.FXML;
//import javafx.stage.WindowEvent;
//
//import java.util.List;
//
//import static atlantafx.base.controls.Popover.ArrowLocation.BOTTOM_LEFT;
//import static javafx.stage.PopupWindow.AnchorLocation.CONTENT_TOP_LEFT;
//
///**
// * 数据过滤业务
// *
// * @author oyzh
// * @since 2024/06/26
// */
//@PopupAttribute(
//        value = FXConst.POPUP_PATH + "sqlserver/shellSqlServerTableRecordFilterPopup.fxml",
//        arrowLocation = BOTTOM_LEFT,
//        anchorLocation = CONTENT_TOP_LEFT
//)
//public class SqlServerTableRecordFilterPopupController extends PopupController {
//
//    /**
//     * 表过滤条件表单
//     */
//    @FXML
//    private FXTableView<SqlServerRecordFilter> filterTable;
//
//    // /**
//    //  * 字段列
//    //  */
//    // @FXML
//    // private FXTableColumn<SqlServerRecordFilter, SqlServerConditionComboBox> column;
//    //
//    // /**
//    //  * 条件列
//    //  */
//    // @FXML
//    // private FXTableColumn<SqlServerRecordFilter, SqlServerConditionComboBox> condition;
//    //
//    // /**
//    //  * 值列
//    //  */
//    // @FXML
//    // private FXTableColumn<SqlServerRecordFilter, Node> value;
//    //
//    // /**
//    //  * 启用列
//    //  */
//    // @FXML
//    // private FXTableColumn<SqlServerRecordFilter, CheckBox> enabled;
//    //
//    // /**
//    //  * 连接符列
//    //  */
//    // @FXML
//    // private FXTableColumn<SqlServerRecordFilter, ComboBox<String>> joinSymbol;
//
//    /**
//     * db表节点
//     */
//    private ShellSqlServerTableTreeItem treeItem;
//
//    /**
//     * 字段列表
//     */
//    private List<SqlServerColumn> columnList;
//
//    /**
//     * 应用
//     */
//    @FXML
//    private void apply() {
//        try {
//            // SqlServerEventUtil.tableFiltered(this.treeItem, this.filterTable.getItems());
//            // this.setProp("filters", this.filterTable.getItems());
//            this.submit(this.filterTable.getItems());
//            this.closeWindow();
//        } catch (Exception ex) {
//            MessageBox.exception(ex, this.getWindow());
//        }
//    }
//
//    /**
//     * 关闭
//     */
//    @FXML
//    private void close() {
//        this.closeWindow();
//    }
//
//    @Override
//    protected void bindListeners() {
//        super.bindListeners();
//    }
//
//    @Override
//    public void onWindowShowing(WindowEvent event) {
//        super.onWindowShowing(event);
//        this.treeItem = this.getProp("item");
//        List<SqlServerRecordFilter> filters = this.getProp("filters");
//        this.filterTable.setItem(filters);
//    }
//
//    @Override
//    public void onWindowHidden(WindowEvent event) {
//        super.onWindowHidden(event);
//        this.columnList = null;
//    }
//
//    // /**
//    //  * 初始化列表控件
//    //  */
//    // private void initTable() {
//    //     this.value.setCellValueFactory(new PropertyValueFactory<>("valueControl"));
//    //     this.column.setCellValueFactory(new PropertyValueFactory<>("columnControl"));
//    //     this.enabled.setCellValueFactory(new PropertyValueFactory<>("enabledControl"));
//    //     this.condition.setCellValueFactory(new PropertyValueFactory<>("conditionControl"));
//    //     this.joinSymbol.setCellValueFactory(new PropertyValueFactory<>("joinSymbolControl"));
//    // }
//
//    // @Override
//    // public void onPopupInitialize(PopupAdapter window) {
//    //     super.onPopupInitialize(window);
//    //     // 初始化表单
//    //     this.initTable();
//    // }
//
//    /**
//     * 添加过滤条件
//     */
//    @FXML
//    private void addFilter() {
//        SqlServerRecordFilter filter = new SqlServerRecordFilter();
//        if (this.columnList == null) {
//            this.columnList = this.treeItem.columns();
//        }
//        filter.setColumns(this.columnList);
//        this.filterTable.addItem(filter);
//    }
//
//    /**
//     * 删除过滤条件
//     */
//    @FXML
//    private void deleteFilter() {
//        try {
//            SqlServerRecordFilter filter = this.filterTable.getSelectedItem();
//            if (filter != null) {
//                this.filterTable.getItems().remove(filter);
//            }
//        } catch (Exception ex) {
//            MessageBox.exception(ex, this.getWindow());
//        }
//    }
//}
