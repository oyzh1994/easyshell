package cn.oyzh.easyshell.controller;

import cn.oyzh.common.SysConst;
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
import cn.oyzh.fx.plus.theme.Themes;
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
         ThemeStyle target;
         if (current.isDarkMode()) {
             if (current == Themes.PRIMER_DARK) {
                 target = Themes.PRIMER_LIGHT;
             } else if (current == Themes.NORD_DARK) {
                 target = Themes.NORD_LIGHT;
             } else if (current == Themes.CUPERTINO_DARK) {
                 target = Themes.CUPERTINO_LIGHT;
             } else if (current == Themes.INTELLIJ_DARK) {
                 target = Themes.INTELLIJ_LIGHT;
             } else if (current == Themes.VSCODE_DARK) {
                 target = Themes.VSCODE_LIGHT;
             } else if (current == Themes.CYBERPUNK_DARK) {
                 target = Themes.CYBERPUNK_LIGHT;
             } else if (current == Themes.LIQUID_GLASS_DARK) {
                 target = Themes.LIQUID_GLASS_LIGHT;
             } else if (current == Themes.ANIME_WARM_DARK) {
                 target = Themes.ANIME_WARM_LIGHT;
             } else if (current == Themes.BUSINESS_DARK) {
                 target = Themes.BUSINESS_LIGHT;
             } else {
                 target = Themes.PRIMER_LIGHT;
             }
         } else {
             if (current == Themes.PRIMER_LIGHT) {
                 target = Themes.PRIMER_DARK;
             } else if (current == Themes.NORD_LIGHT) {
                 target = Themes.NORD_DARK;
             } else if (current == Themes.CUPERTINO_LIGHT) {
                 target = Themes.CUPERTINO_DARK;
             } else if (current == Themes.INTELLIJ_LIGHT) {
                 target = Themes.INTELLIJ_DARK;
             } else if (current == Themes.VSCODE_LIGHT) {
                 target = Themes.VSCODE_DARK;
             } else if (current == Themes.CYBERPUNK_LIGHT) {
                 target = Themes.CYBERPUNK_DARK;
             } else if (current == Themes.LIQUID_GLASS_LIGHT) {
                 target = Themes.LIQUID_GLASS_DARK;
             } else if (current == Themes.ANIME_WARM_LIGHT) {
                 target = Themes.ANIME_WARM_DARK;
             } else if (current == Themes.BUSINESS_LIGHT) {
                 target = Themes.BUSINESS_DARK;
             } else {
                 target = Themes.PRIMER_DARK;
             }
         }
         ThemeManager.apply(target);
         ShellSetting setting=ShellSettingStore.SETTING;
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
