package cn.oyzh.easyshell.popups.mysql;

import cn.oyzh.easyshell.filter.mysql.ShellMysqlDataFilterParam;
import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.fx.plus.controller.PopupController;
import cn.oyzh.fx.plus.controls.button.FXCheckBox;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.window.PopupAttribute;
import javafx.fxml.FXML;
import javafx.stage.WindowEvent;

/**
 * mysql过滤设置弹窗
 *
 * @author oyzh
 * @since 2025/11/07
 */
@PopupAttribute(
        value = FXConst.POPUP_PATH + "mysql/shellMysqlFilterSettingPopup.fxml"
)
public class ShellMysqlFilterSettingPopupController extends PopupController {

    /**
     * 匹配大小写
     */
    @FXML
    private FXCheckBox matchCase;

    /**
     * 匹配全文
     */
    @FXML
    private FXCheckBox matchFull;

    /**
     * 应用
     */
    @FXML
    private void apply() {
        try {
            ShellMysqlDataFilterParam filterParam = new ShellMysqlDataFilterParam();
            filterParam.setMatchCase(this.matchCase.isSelected());
            filterParam.setMatchFull(this.matchFull.isSelected());
            this.submit(filterParam);
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
    public void onWindowShowing(WindowEvent event) {
        super.onWindowShowing(event);
        // 初始化
        ShellMysqlDataFilterParam filterParam = this.getProp("filterParam");
        if (filterParam != null) {
            this.matchCase.setSelected(filterParam.isMatchCase());
            this.matchFull.setSelected(filterParam.isMatchFull());
        }
    }
}
