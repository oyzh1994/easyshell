package cn.oyzh.easyshell.popups.mysql;

import cn.oyzh.easyshell.fx.mysql.table.MysqlIndexColumnListView;
import cn.oyzh.easyshell.mysql.column.MysqlColumn;
import cn.oyzh.easyshell.mysql.index.MysqlIndex;
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
        value = FXConst.POPUP_PATH + "mysql/shellMysqlIndexFieldPopup.fxml"
)
public class MysqlIndexFieldPopupController extends PopupController {

    /**
     * 提交事件
     */
    private Runnable onSubmit;

    /**
     * 值组件
     */
    @FXML
    private MysqlIndexColumnListView listView;

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
     * 添加行
     */
    @FXML
    private void addRow() {
        this.listView.addColumn(new MysqlIndex.IndexColumn());
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
        MysqlIndex dbIndex = this.getProp("dbIndex");
        List<MysqlColumn> columnList = this.getProp("columnList");
        this.listView.init(dbIndex, columnList);
    }
}
