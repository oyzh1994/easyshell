package cn.oyzh.easyshell.controller;

import cn.oyzh.common.dto.Project;
import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.system.OSUtil;
import cn.oyzh.easyshell.domain.ShellSetting;
import cn.oyzh.easyshell.event.ShellEventUtil;
import cn.oyzh.easyshell.store.ShellSettingStore;
import cn.oyzh.easyshell.util.ShellViewFactory;
import cn.oyzh.event.EventSubscribe;
import cn.oyzh.fx.gui.event.Layout1Event;
import cn.oyzh.fx.gui.event.Layout2Event;
import cn.oyzh.fx.gui.svg.pane.LayoutSVGPane;
import cn.oyzh.fx.plus.controller.StageController;
import cn.oyzh.fx.plus.controls.FXHeaderBar;
import cn.oyzh.fx.plus.font.FontManager;
import cn.oyzh.fx.plus.font.FontUtil;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.menu.FXMenu;
import cn.oyzh.fx.plus.menu.FXMenuItem;
import cn.oyzh.fx.plus.theme.ThemeManager;
import cn.oyzh.fx.plus.theme.ThemeStyle;
import cn.oyzh.fx.plus.theme.ThemeUtil;
import cn.oyzh.fx.plus.theme.Themes;
import cn.oyzh.fx.plus.window.StageAdapter;
import cn.oyzh.fx.plus.window.StageManager;
import cn.oyzh.i18n.I18nHelper;
import javafx.fxml.FXML;
import javafx.scene.control.MenuBar;
import javafx.stage.WindowEvent;

import java.util.Locale;

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
     * 头部组件
     */
    @FXML
    private FXHeaderBar root;

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
    private void tools() {
        ShellViewFactory.tool();
    }

    /**
     * 主题切换
     */
    @FXML
    private void themeToggle() {
        ThemeStyle current = ThemeManager.currentTheme();
        ThemeStyle target = ThemeUtil.getInverseTheme(current);
        this.setting.setTheme(target.getName());
        this.setting.setBgColor(target.getBackgroundColorHex());
        this.setting.setFgColor(target.getForegroundColorHex());
        this.setting.setAccentColor(target.getAccentColorHex());
        ShellSettingStore.INSTANCE.replace(ShellSettingStore.SETTING);
        ThemeManager.apply(target);
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

    @Override
    public void onStageInitialize(StageAdapter stage) {
        super.onStageInitialize(stage);
        if (OSUtil.isMacOS()) {
            MenuBar menuBar = new MenuBar();

            FXMenu easyshell = new FXMenu(Project.load().getName());
            menuBar.getMenus().add(easyshell);

            FXMenuItem hiddenLeftSide = new FXMenuItem(I18nHelper.hiddenLeftSide(), ShellEventUtil::layout1);
            easyshell.getItems().add(hiddenLeftSide);

            FXMenuItem showLeftSide = new FXMenuItem(I18nHelper.showLeftSide(), ShellEventUtil::layout2);
            easyshell.getItems().add(showLeftSide);

            FXMenuItem addConnect = new FXMenuItem(I18nHelper.addConnect(), ShellViewFactory::addConnectGuid);
            easyshell.getItems().add(addConnect);

            FXMenuItem addFolder1 = new FXMenuItem(I18nHelper.addFolder1(), ShellEventUtil::addGroup);
            easyshell.getItems().add(addFolder1);

            FXMenuItem exportData = new FXMenuItem(I18nHelper.exportData(), ShellViewFactory::dataExport);
            easyshell.getItems().add(exportData);

            FXMenuItem importData = new FXMenuItem(I18nHelper.importData(), ShellViewFactory::dataImport);
            easyshell.getItems().add(importData);

            FXMenuItem minimize = new FXMenuItem(I18nHelper.minimize(), () -> {
                StageManager.getPrimaryStage().setIconified(true);
            });
            easyshell.getItems().add(minimize);

            FXMenu features1 = new FXMenu(I18nHelper.features1());
            menuBar.getMenus().add(features1);

            FXMenuItem key1 = new FXMenuItem(I18nHelper.key1(), this::key);
            features1.getItems().add(key1);

            FXMenuItem snippet = new FXMenuItem(I18nHelper.snippet(), this::snippet);
            features1.getItems().add(snippet);

            FXMenuItem message = new FXMenuItem(I18nHelper.message(), this::message);
            features1.getItems().add(message);

            FXMenuItem tools = new FXMenuItem(I18nHelper.tools(), this::tools);
            features1.getItems().add(tools);

            FXMenuItem termSplitView = new FXMenuItem(I18nHelper.termSplitView(), ShellViewFactory::splitGuid);
            features1.getItems().add(termSplitView);

            FXMenuItem localTerminal = new FXMenuItem(I18nHelper.localTerminal(), ShellEventUtil::showTerminal);
            features1.getItems().add(localTerminal);

            FXMenuItem changelog = new FXMenuItem(I18nHelper.changelog(), ShellEventUtil::changelog);
            features1.getItems().add(changelog);

            FXMenu theme = new FXMenu(I18nHelper.theme());
            menuBar.getMenus().add(theme);

            for (ThemeStyle style : Themes.allThemes()) {
                FXMenuItem menuItem = new FXMenuItem(style.getDesc(Locale.getDefault()), () -> {
                    this.setting.setTheme(style.getName());
                    this.setting.setBgColor(style.getBackgroundColorHex());
                    this.setting.setFgColor(style.getForegroundColorHex());
                    this.setting.setAccentColor(style.getAccentColorHex());
                    ShellSettingStore.INSTANCE.replace(ShellSettingStore.SETTING);
                    ThemeManager.apply(style);
                });
                theme.getItems().add(menuItem);
            }
            FXMenuItem toggleTheme = new FXMenuItem(I18nHelper.toggleTheme(), this::themeToggle);
            theme.getItems().add(toggleTheme);

            FXMenu font = new FXMenu(I18nHelper.font());
            menuBar.getMenus().add(font);

            for (String f : FontUtil.getFamilies()) {
                FXMenuItem menuItem = new FXMenuItem(f, () -> {
                    this.setting.setFontFamily(f);
                    ShellSettingStore.INSTANCE.replace(this.setting);
                    FontManager.apply(this.setting.fontConfig());
                });
                font.getItems().add(menuItem);
            }

            FXMenu help = new FXMenu(I18nHelper.help());
            menuBar.getMenus().add(help);

            FXMenuItem about = new FXMenuItem(I18nHelper.about(), this::about);
            help.getItems().add(about);

            FXMenuItem setting = new FXMenuItem(I18nHelper.setting(), this::setting);
            help.getItems().add(setting);

            FXMenuItem quit = new FXMenuItem(I18nHelper.quit(), this::quit);
            help.getItems().add(quit);

//            FXMenuItem restart = new FXMenuItem(I18nHelper.restart1(), ShellProcessUtil::restartApplication);
//            help.getItems().add(restart);

            menuBar.setUseSystemMenuBar(true);
            this.root.setRight(menuBar);
        }
    }
}
