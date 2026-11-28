package cn.oyzh.easyshell.popups.h2;

import cn.oyzh.easyshell.h2.column.H2Column;
import cn.oyzh.easyshell.h2.record.H2RecordFilter;
import cn.oyzh.easyshell.trees.h2.table.ShellH2TableTreeItem;
import cn.oyzh.easyshell.trees.h2.view.ShellH2ViewTreeItem;
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
 * H2 记录过滤条件弹窗。
 *
 * @since 2026-11-02
 */
@PopupAttribute(
        value = FXConst.POPUP_PATH + "h2/shellH2RecordFilterPopup.fxml"
)
public class ShellH2RecordFilterPopupController extends PopupController {

    @FXML
    private FXTableView<H2RecordFilter> filterTable;

    /**
     * 数据库表或视图节点
     */
    private TreeItem<?> treeItem;

    /**
     * 字段列表
     */
    private List<H2Column> columnList;

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
        List<H2RecordFilter> filters = this.getProp("filters");
        this.filterTable.setItem(filters == null ? new ArrayList<>() : filters);
    }

    @Override
    public void onWindowHidden(WindowEvent event) {
        super.onWindowHidden(event);
        this.columnList = null;
    }

    @FXML
    private void addFilter() {
        H2RecordFilter filter = new H2RecordFilter();
        if (this.columnList == null) {
            if (this.treeItem instanceof ShellH2TableTreeItem item) {
                this.columnList = item.columns();
            } else if (this.treeItem instanceof ShellH2ViewTreeItem item) {
                this.columnList = item.columns();
            }
        }
        filter.setColumns(this.columnList);
        this.filterTable.addItem(filter);
    }

    @FXML
    private void deleteFilter() {
        H2RecordFilter filter = this.filterTable.getSelectedItem();
        if (filter != null) {
            this.filterTable.getItems().remove(filter);
        }
    }
}
