package cn.oyzh.easyshell.controller;

import cn.oyzh.common.SysConst;
import cn.oyzh.common.system.OSUtil;
import cn.oyzh.easyshell.event.ShellEventUtil;
import cn.oyzh.easyshell.util.ShellViewFactory;
import cn.oyzh.event.EventSubscribe;
import cn.oyzh.fx.gui.event.Layout1Event;
import cn.oyzh.fx.gui.event.Layout2Event;
import cn.oyzh.fx.gui.svg.pane.LayoutSVGPane;
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
     * 布局组件
     */
    @FXML
    private LayoutSVGPane layoutPane;

    /**
     * 设置
     */
    @FXML
    private void setting() {
//        ShellEventUtil.showSetting();
        ShellViewFactory.setting();
    }

    /**
     * 关于
     */
    @FXML
    private void about() {
//        ShellEventUtil.showAbout();
        ShellViewFactory.about();
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
     * 传输数据
     */
    @FXML
    private void transport() {
//        ShellEventUtil.showTransportFile();
        ShellViewFactory.fileTransport(null);
    }

    /**
     * 密钥
     */
    @FXML
    private void key() {
        ShellEventUtil.showKey();
    }

    /**
     * 消息
     */
    @FXML
    private void message() {
        ShellEventUtil.layout2();
        ShellEventUtil.showMessage();
    }

    /**
     * 工具箱
     */
    @FXML
    private void tool() {
//        ShellEventUtil.showTool();
        ShellViewFactory.tool();
    }

    /**
     * 布局
     */
    @FXML
    private void layout() {
        if (!this.layoutPane.isLayout1()) {
            ShellEventUtil.layout2();
        } else {
            ShellEventUtil.layout1();
        }
    }

    /**
     * 布局1事件
     *
     * @param event 事件
     */
    @EventSubscribe
    private void layout1(Layout1Event event) {
        this.layoutPane.layout2();
    }

    /**
     * 布局2事件
     *
     * @param event 事件
     */
    @EventSubscribe
    private void layout2(Layout2Event event) {
        this.layoutPane.layout1();
    }

//    /**
//     * 布局1
//     */
//    @FXML
//    private void layout1() {
//        ShellEventUtil.layout1();
//    }
//
//    /**
//     * 布局2
//     */
//    @FXML
//    private void layout2() {
//        ShellEventUtil.layout2();
//    }

    /**
     * 分割面板
     */
    @FXML
    private FXPane splitPane;

    @Override
    public void onWindowShowing(WindowEvent event) {
        super.onWindowShowing(event);
        if (OSUtil.isWindows()) {
            this.splitPane.setFlexHeight("100% - 282");
        }
    }
}
