package cn.oyzh.easyshell.popups.mysql;

import cn.oyzh.easyshell.fx.mysql.table.MysqlColumnListView;
import cn.oyzh.easyshell.mysql.column.MysqlColumn;
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
 * @since 2024/07/12
 */
@PopupAttribute(
        value = FXConst.POPUP_PATH + "mysql/shellMysqlColumnFieldPopup.fxml"
)
public class MysqlColumnFieldPopupController extends PopupController {

    /**
     * 提交事件
     */
    private Runnable onSubmit;

    /**
     * 值组件
     */
    @FXML
    private MysqlColumnListView listView;

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
        List<MysqlColumn> columns = this.getProp("columns");
        List<String> selectedColumns = this.getProp("selectedColumns");
        this.listView.init(columns);
        this.listView.select(selectedColumns);
    }
}
