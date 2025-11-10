package cn.oyzh.easyshell.popups.mysql;

import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.fx.plus.controller.PopupController;
import cn.oyzh.fx.plus.controls.button.FXCheckBox;
import cn.oyzh.fx.plus.controls.list.FXListView;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.window.PopupAdapter;
import cn.oyzh.fx.plus.window.PopupAttribute;
import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.stage.WindowEvent;

import java.util.List;

/**
 * 数据枚举弹窗业务
 *
 * @author oyzh
 * @since 2024/07/17
 */
@PopupAttribute(
        value = FXConst.POPUP_PATH + "mysql/shellMysqlRecordEnumPopup.fxml"
)
public class ShellMysqlRecordEnumPopupController extends PopupController {

    /**
     * 提交事件
     */
    private Runnable onSubmit;

    /**
     * 值组件
     */
    @FXML
    private FXListView<CheckBox> listView;

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

    @Override
    protected void bindListeners() {
        super.bindListeners();
    }

    @Override
    public void onWindowShowing(WindowEvent event) {
        super.onWindowShowing(event);
        this.onSubmit = this.getProp("onSubmit");
        List<String> values = this.getProp("values");
        List<String> allValues = this.getProp("allValues");
        for (String value : allValues) {
            FXCheckBox checkBox = new FXCheckBox(value);
            if (values.contains(value)) {
                checkBox.setSelected(true);
            }
            this.listView.addItem(checkBox);
        }
    }

    @Override
    public void onPopupInitialize(PopupAdapter window) {
        super.onPopupInitialize(window);
    }
}
