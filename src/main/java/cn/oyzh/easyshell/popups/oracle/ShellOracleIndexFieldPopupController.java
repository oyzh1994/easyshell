package cn.oyzh.easyshell.popups.oracle;

import cn.oyzh.easyshell.oracle.column.OracleColumn;
import cn.oyzh.easyshell.oracle.index.OracleIndex;
import cn.oyzh.easyshell.fx.oracle.table.ShellOracleIndexColumnListView;
import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.fx.plus.controller.PopupController;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.util.ListViewUtil;
import cn.oyzh.fx.plus.window.PopupAttribute;
import javafx.fxml.FXML;
import javafx.stage.WindowEvent;

import java.util.List;

/**
 * 字段列表弹窗业务
 *
 * @author oyzh
 * @since 2025-11-06
 */
@PopupAttribute(
        value = FXConst.POPUP_PATH + "oracle/shellOracleIndexFieldPopup.fxml"
)
public class ShellOracleIndexFieldPopupController extends PopupController {

    /**
     * 提交事件
     */
    private Runnable onSubmit;

    /**
     * 值组件
     */
    @FXML
    private ShellOracleIndexColumnListView listView;

    /**
     * 提交
     */
    @FXML
    private void submit() {
        try {
            if (this.onSubmit != null) {
                this.onSubmit.run();
            }
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

    /**
     * 添加行
     */
    @FXML
    private void addRow() {
        this.listView.addColumn(new OracleIndex.IndexColumn());
        this.listView.selectLast();
    }

    /**
     * 删除行
     */
    @FXML
    private void deleteRow() {
        this.listView.removeSelectedItem();
    }

    /**
     * 上移行
     */
    @FXML
    private void moveUpRow() {
        ListViewUtil.moveUp(this.listView);
    }

    /**
     * 下移行
     */
    @FXML
    private void moveDownRow() {
        ListViewUtil.moveDown(this.listView);
    }

    @Override
    public void onWindowShowing(WindowEvent event) {
        super.onWindowShowing(event);
        this.onSubmit = this.getProp("onSubmit");
        OracleIndex dbIndex = this.getProp("dbIndex");
        List<OracleColumn> columnList = this.getProp("columnList");
        this.listView.init(dbIndex, columnList);
    }
}
