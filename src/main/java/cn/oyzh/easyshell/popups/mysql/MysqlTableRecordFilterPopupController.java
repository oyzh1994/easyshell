package cn.oyzh.easyshell.popups.mysql;

import cn.oyzh.easyshell.mysql.column.MysqlColumn;
import cn.oyzh.easyshell.mysql.record.MysqlRecordFilter;
import cn.oyzh.easyshell.trees.mysql.table.MysqlTableTreeItem;
import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.fx.plus.controller.PopupController;
import cn.oyzh.fx.plus.controls.table.FXTableView;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.window.PopupAttribute;
import javafx.fxml.FXML;
import javafx.stage.WindowEvent;

import java.util.List;

import static atlantafx.base.controls.Popover.ArrowLocation.BOTTOM_LEFT;
import static javafx.stage.PopupWindow.AnchorLocation.CONTENT_TOP_LEFT;

/**
 * 数据过滤业务
 *
 * @author oyzh
 * @since 2024/06/26
 */
@PopupAttribute(
        value = FXConst.POPUP_PATH + "mysqlTableRecordFilterPopup.fxml",
        arrowLocation = BOTTOM_LEFT,
        anchorLocation = CONTENT_TOP_LEFT
)
public class MysqlTableRecordFilterPopupController extends PopupController {

    /**
     * 表过滤条件表单
     */
    @FXML
    private FXTableView<MysqlRecordFilter> filterTable;

    // /**
    //  * 字段列
    //  */
    // @FXML
    // private FXTableColumn<MysqlRecordFilter, MysqlConditionComboBox> column;
    //
    // /**
    //  * 条件列
    //  */
    // @FXML
    // private FXTableColumn<MysqlRecordFilter, MysqlConditionComboBox> condition;
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
    private MysqlTableTreeItem treeItem;

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
            this.columnList = this.treeItem.columns();
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
