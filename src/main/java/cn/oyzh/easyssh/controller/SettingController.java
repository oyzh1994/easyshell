package cn.oyzh.easyssh.controller;


import cn.oyzh.easyfx.controller.FXController;
import cn.oyzh.easyfx.controls.FXToggleGroup;
import cn.oyzh.easyfx.controls.FlexCheckBox;
import cn.oyzh.easyfx.information.FXToastUtil;
import cn.oyzh.easyfx.view.FXWindow;
import cn.oyzh.easyssh.SSHConst;
import cn.oyzh.easyssh.SSHStyle;
import cn.oyzh.easyssh.domain.SSHSetting;
import cn.oyzh.easyssh.store.SSHSettingStore;
import javafx.fxml.FXML;
import javafx.scene.control.RadioButton;
import javafx.stage.Modality;
import javafx.stage.WindowEvent;

/**
 * 应用设置业务
 *
 * @author oyzh
 * @since 2023/06/16
 */
@FXWindow(
        title = "应用设置",
        iconUrls = SSHConst.ICON_PATH,
        modality = Modality.APPLICATION_MODAL,
        cssUrls = SSHStyle.COMMON,
        value = SSHConst.FXML_BASE_PATH + "setting.fxml"
)
public class SettingController extends FXController {

    /**
     * 退出方式
     */
    @FXML
    private FXToggleGroup exitMode;

    /**
     * 退出方式0
     */
    @FXML
    private RadioButton exitMode0;

    /**
     * 退出方式1
     */
    @FXML
    private RadioButton exitMode1;

    /**
     * 退出方式2
     */
    @FXML
    private RadioButton exitMode2;

    /**
     * 记住页面大小
     */
    @FXML
    private FlexCheckBox pageSize;

    /**
     * 记住页面拉伸
     */
    @FXML
    private FlexCheckBox pageResize;

    /**
     * 记住页面位置
     */
    @FXML
    private FlexCheckBox pageLocation;

    /**
     * 配置对象
     */
    private final SSHSetting setting = SSHSettingStore.SETTING;

    /**
     * 配置持久化对象
     */
    private final SSHSettingStore settingStore = SSHSettingStore.INSTANCE;

    @Override
    public void onViewShowing(WindowEvent event) {
        super.onViewShowing(event);
        // 应用退出处理
        if (this.setting.getExitMode() != null) {
            switch (this.setting.getExitMode()) {
                case 0 -> this.exitMode0.setSelected(true);
                case 1 -> this.exitMode1.setSelected(true);
                case 2 -> this.exitMode2.setSelected(true);
            }
        }

        // 记住页面大小处理
        if (this.setting.getPageInfo() != null) {
            this.pageSize.setSelected(this.setting.isRememberPageSize());
        }

        // 记住页面拉伸处理
        if (this.setting.getRememberPageResize() != null) {
            this.pageResize.setSelected(this.setting.isRememberPageResize());
        }

        // 记住页面位置处理
        if (this.setting.getRememberPageLocation() != null) {
            this.pageLocation.setSelected(this.setting.isRememberPageLocation());
        }
    }

    /**
     * 保存设置
     */
    @FXML
    private void saveSetting() {
        String tips = "";
        // 设置参数
        this.setting.setPageInfo(this.pageSize.isSelected() ? 1 : 0);
        this.setting.setRememberPageResize(this.pageResize.isSelected() ? 1 : 0);
        this.setting.setRememberPageLocation(this.pageLocation.isSelected() ? 1 : 0);
        this.setting.setExitMode(Integer.parseInt(this.exitMode.selectedUserData()));
        if (this.settingStore.update(this.setting)) {
            FXToastUtil.ok("保存配置成功" + tips);
            this.closeView();
        } else {
            FXToastUtil.warn("保存配置失败！");
        }
    }

    @Override
    public void onViewShown(WindowEvent event) {
        this.view.hideOnEscape();
    }
}
