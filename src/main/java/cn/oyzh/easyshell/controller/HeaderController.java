package cn.oyzh.easyshell.controller;

import cn.oyzh.common.dto.Project;
import cn.oyzh.common.log.JulLog;
import cn.oyzh.easyshell.domain.ShellSetting;
import cn.oyzh.easyshell.event.ShellEventUtil;
import cn.oyzh.easyshell.store.ShellSettingStore;
import cn.oyzh.easyshell.util.ShellViewFactory;
import cn.oyzh.event.EventSubscribe;
import cn.oyzh.fx.gui.event.Layout1Event;
import cn.oyzh.fx.gui.event.Layout2Event;
import cn.oyzh.fx.gui.svg.pane.LayoutSVGPane;
import cn.oyzh.fx.plus.controller.StageController;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.theme.ThemeManager;
import cn.oyzh.fx.plus.theme.ThemeStyle;
import cn.oyzh.fx.plus.theme.ThemeUtil;
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
     * 项目信息
     */
    private final Project project = Project.load();

    /**
     * shell相关配置
     */
    private final ShellSetting setting = ShellSettingStore.SETTING;

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
//        if (MessageBox.confirm(I18nHelper.quit() + " " + SysConst.projectName())) {
//            StageManager.exit();
//        }
        // 直接退出应用
        if (this.setting.isExitDirectly()) {
            JulLog.info("exit directly.");
            StageManager.exit();
        } else { // 总是询问
            if (MessageBox.confirm(I18nHelper.quit() + " " + this.project.getName())) {
                JulLog.info("exit by confirm.");
                StageManager.exit();
            } else {
                JulLog.info("cancel by confirm.");
            }
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
        // ShellEventUtil.layout2();
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
     * 主题切换
     */
    @FXML
    private void themeToggle() {
        ThemeStyle current = ThemeManager.currentTheme();
        ThemeStyle target = ThemeUtil.getInverseTheme(current);
        ThemeManager.apply(target);
        ShellSetting setting = ShellSettingStore.SETTING;
        setting.setTheme(target.getName());
        setting.setBgColor(target.getBackgroundColorHex());
        setting.setFgColor(target.getForegroundColorHex());
        setting.setAccentColor(target.getAccentColorHex());
        ShellSettingStore.INSTANCE.replace(ShellSettingStore.SETTING);
    }

    /**
     * 布局
     */
    @FXML
    private void layout() {
        if (this.layoutPane.isLayout1()) {
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
        this.layoutPane.layout1();
    }

    /**
     * 布局2事件
     *
     * @param event 事件
     */
    @EventSubscribe
    private void layout2(Layout2Event event) {
        this.layoutPane.setTipText(I18nHelper.hiddenLeftSide());
        this.layoutPane.layout2();
    }

    @Override
    public void onWindowShowing(WindowEvent event) {
        super.onWindowShowing(event);
        this.layoutPane.setTipText(I18nHelper.hiddenLeftSide());
    }
}
