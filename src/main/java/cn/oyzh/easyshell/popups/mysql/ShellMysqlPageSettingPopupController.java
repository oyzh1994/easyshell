package cn.oyzh.easyshell.popups.mysql;

import cn.oyzh.easyshell.domain.ShellSetting;
import cn.oyzh.easyshell.store.ShellSettingStore;
import cn.oyzh.fx.gui.text.field.NumberTextField;
import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.fx.plus.controller.PopupController;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.window.PopupAttribute;
import javafx.fxml.FXML;
import javafx.stage.WindowEvent;

import static atlantafx.base.controls.Popover.ArrowLocation.BOTTOM_LEFT;
import static javafx.stage.PopupWindow.AnchorLocation.CONTENT_TOP_LEFT;

/**
 * 页码设置弹窗
 *
 * @author oyzh
 * @since 2024/08/06
 */
@PopupAttribute(
        value = FXConst.POPUP_PATH + "mysql/shellMysqlPageSettingPopup.fxml",
        arrowLocation = BOTTOM_LEFT,
        anchorLocation = CONTENT_TOP_LEFT
)
public class ShellMysqlPageSettingPopupController extends PopupController {

    /**
     * 每页限制
     */
    @FXML
    private NumberTextField limit;

    /**
     * 设置
     */
    private final ShellSetting setting = ShellSettingStore.SETTING;

    /**
     * 设置存储
     */
    private final ShellSettingStore settingStore = ShellSettingStore.INSTANCE;

    /**
     * 应用
     */
    @FXML
    private void apply() {
        try {
            int limit = this.limit.getIntValue();
            this.setting.setRecordPageLimit(limit);
            this.settingStore.update(this.setting);
            this.submit(limit);
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
        this.limit.setValue(this.setting.getRecordPageLimit());
    }

    @Override
    public void onWindowHidden(WindowEvent event) {
        super.onWindowHidden(event);
        this.limit = null;
    }
}
