package cn.oyzh.easyshell.controller;

import cn.oyzh.common.SysConst;
import cn.oyzh.easyshell.event.ShellEventUtil;
import cn.oyzh.easyshell.util.ShellViewFactory;
import cn.oyzh.event.EventSubscribe;
import cn.oyzh.fx.gui.event.Layout1Event;
import cn.oyzh.fx.gui.event.Layout2Event;
import cn.oyzh.fx.gui.svg.pane.LayoutSVGPane2;
import cn.oyzh.fx.plus.controller.StageController;
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
public class HeaderController2 extends StageController {

    /**
     * 布局组件
     */
    @FXML
    private LayoutSVGPane2 layoutPane;

    /**
     * 设置
     */
    @FXML
    private void setting() {
        ShellViewFactory.setting();
    }

    /**
     * 关于
     */
    @FXML
    private void about() {
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
     * 片段
     */
    @FXML
    private void snippet() {
        ShellViewFactory.snippet();
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
        this.layoutPane.setTipText(I18nHelper.showLeftSide());
        this.layoutPane.layout2();
    }

    /**
     * 布局2事件
     *
     * @param event 事件
     */
    @EventSubscribe
    private void layout2(Layout2Event event) {
        this.layoutPane.setTipText(I18nHelper.hiddenLeftSide());
        this.layoutPane.layout1();
    }

    @Override
    public void onWindowShowing(WindowEvent event) {
        super.onWindowShowing(event);
        this.layoutPane.setTipText(I18nHelper.hiddenLeftSide());
    }
}
