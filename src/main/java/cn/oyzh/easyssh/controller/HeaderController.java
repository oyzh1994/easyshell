package cn.oyzh.easyssh.controller;

import cn.oyzh.common.SysConst;
import cn.oyzh.common.system.OSUtil;
import cn.oyzh.easyssh.event.SSHEventUtil;
import cn.oyzh.fx.plus.controller.StageController;
import cn.oyzh.fx.plus.controls.pane.FXPane;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.window.StageManager;
import cn.oyzh.i18n.I18nHelper;
import javafx.fxml.FXML;
import javafx.stage.WindowEvent;

/**
 * 主页头部业务
 *
 * @author oyzh
 * @since 2022/1/26
 */
public class HeaderController extends StageController {

    /**
     * 设置
     */
    @FXML
    private void setting() {
        SSHEventUtil.showSetting();
    }

    /**
     * 关于
     */
    @FXML
    private void about() {
        SSHEventUtil.showAbout();
    }

    /**
     * 退出
     */
    @FXML
    private void quit() {
        if (MessageBox.confirm(I18nHelper.quit() + " " + SysConst.projectName())) {
            StageManager.exit();
        }
    }

    /**
     * 工具箱
     */
    @FXML
    private void tool() {
        SSHEventUtil.showTool();
    }

    /**
     * 布局1
     */
    @FXML
    private void layout1() {
        SSHEventUtil.layout1();
    }

    /**
     * 布局2
     */
    @FXML
    private void layout2() {
        SSHEventUtil.layout2();
    }

    /**
     * 分割面板
     */
    @FXML
    private FXPane splitPane;

    @Override
    public void onWindowShowing(WindowEvent event) {
        super.onWindowShowing(event);
        if (OSUtil.isWindows() || OSUtil.isLinux()) {
            this.splitPane.setFlexHeight("100% - 180");
        }
    }
}
