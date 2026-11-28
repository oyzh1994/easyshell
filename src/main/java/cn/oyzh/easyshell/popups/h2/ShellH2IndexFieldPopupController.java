package cn.oyzh.easyshell.popups.h2;

import cn.oyzh.easyshell.fx.h2.table.ShellH2IndexColumnListView;
import cn.oyzh.easyshell.h2.column.H2Column;
import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.fx.plus.controller.PopupController;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.util.ListViewUtil;
import cn.oyzh.fx.plus.window.PopupAttribute;
import javafx.fxml.FXML;
import javafx.stage.WindowEvent;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * H2索引字段选择弹窗。
 *
 * @author oyzh
 * @since 2026-11-03
 */
@PopupAttribute(
        value = FXConst.POPUP_PATH + "h2/shellH2IndexFieldPopup.fxml"
)
public class ShellH2IndexFieldPopupController extends PopupController {

    private Runnable onSubmit;

    @FXML
    private ShellH2IndexColumnListView listView;

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

    @FXML
    private void close() {
        this.closeWindow();
    }

    @FXML
    private void addRow() {
        this.listView.addColumn(null);
        this.listView.selectLast();
    }

    @FXML
    private void deleteRow() {
        this.listView.removeSelectedItem();
    }

    @FXML
    private void moveUpRow() {
        ListViewUtil.moveUp(this.listView);
    }

    @FXML
    private void moveDownRow() {
        ListViewUtil.moveDown(this.listView);
    }

    @Override
    public void onWindowShowing(WindowEvent event) {
        super.onWindowShowing(event);
        this.onSubmit = this.getProp("onSubmit");
        List<? extends H2Column> columnList = this.getProp("columnList");
        Collection<String> columns = this.getProp("columns");
        this.listView.init(columnList, columns);
    }
}
