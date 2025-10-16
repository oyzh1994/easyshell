package cn.oyzh.easyshell.controller;

import cn.oyzh.common.dto.Project;
import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.system.OSUtil;
import cn.oyzh.easyshell.domain.ShellSetting;
import cn.oyzh.easyshell.store.ShellSettingStore;
import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.fx.plus.controller.ParentStageController;
import cn.oyzh.fx.plus.controller.StageController;
import cn.oyzh.fx.plus.i18n.I18nResourceBundle;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.tray.TrayManager;
import cn.oyzh.fx.plus.window.FXStageStyle;
import cn.oyzh.fx.plus.window.StageAdapter;
import cn.oyzh.fx.plus.window.StageAttribute;
import cn.oyzh.fx.plus.window.StageManager;
import cn.oyzh.i18n.I18nHelper;
import javafx.fxml.FXML;
import javafx.stage.WindowEvent;

import java.util.Arrays;
import java.util.List;

/**
 * 主页
 *
 * @author oyzh
 * @since 2022/8/19
 */
@StageAttribute(
        usePrimary = true,
        fullScreenAble = true,
        alwaysOnTopAble = true,
        stageStyle = FXStageStyle.EXTENDED,
        value = FXConst.FXML_PATH + "main.fxml"
)
public class MainController extends ParentStageController {

    /**
     * 项目信息
     */
    private final Project project = Project.load();

    /**
     * 头部页面
     */
    @FXML
    private HeaderController2 headerController;

    /**
     * shell主页业务
     */
    @FXML
    private ShellMainController shellMainController;

    /**
     * shell相关配置
     */
    private final ShellSetting setting = ShellSettingStore.SETTING;

    /**
     * 设置存储
     */
    private final ShellSettingStore settingStore = ShellSettingStore.INSTANCE;

    @Override
    public List<? extends StageController> getSubControllers() {
        return Arrays.asList(this.shellMainController, this.headerController);
    }

    @Override
    public void onWindowCloseRequest(WindowEvent event) {
        JulLog.warn("main view closing.");
        // 直接退出应用
        if (this.setting.isExitDirectly()) {
            if (JulLog.isInfoEnabled()) {
                JulLog.info("exit directly.");
            }
            StageManager.exit();
        } else if (this.setting.isExitAsk()) { // 总是询问
            if (MessageBox.confirm(I18nHelper.quit() + " " + this.project.getName())) {
                if (JulLog.isInfoEnabled()) {
                    JulLog.info("exit by confirm.");
                }
                StageManager.exit();
            } else {
                if (JulLog.isInfoEnabled()) {
                    JulLog.info("cancel by confirm.");
                }
                event.consume();
            }
        } else if (OSUtil.isMacOS()) {// macos单独处理
            // TODO: 注意macos除非退出应用，否则任务栏一直会保留图标，所以macos的托盘选项无效
            if (JulLog.isInfoEnabled()) {
                JulLog.info("exit by macos.");
            }
            StageManager.exit();
        } else if (this.setting.isExitTray()) {// 系统托盘
            if (TrayManager.exist()) {
                if (JulLog.isInfoEnabled()) {
                    JulLog.info("show tray.");
                }
                TrayManager.show();
            } else {
                JulLog.error("tray not support!");
//                event.consume();
//                JulLog.error("tray not support, iconified window");
//                this.stage.setIconified(true);
//                MessageBox.warn(I18nHelper.trayNotSupport());
                StageManager.exit();
            }
        }
    }

    @Override
    public void onSystemExit() {
        boolean savePageInfo = false;
        // 记住页面大小
        if (this.setting.isRememberPageSize()) {
            this.setting.setPageWidth(this.stage.getWidth());
            this.setting.setPageHeight(this.stage.getHeight());
            this.setting.setPageMaximized(this.stage.isMaximized());
            savePageInfo = true;
        }
        // 记住页面位置
        if (this.setting.isRememberPageLocation()) {
            this.setting.setPageScreenX(this.stage.getX());
            this.setting.setPageScreenY(this.stage.getY());
            savePageInfo = true;
        }
        // 保存页面信息
        if (savePageInfo) {
            this.settingStore.replace(this.setting);
        }
        // 关闭托盘
        TrayManager.destroy();
        super.onSystemExit();
    }

    @Override
    public void onStageInitialize(StageAdapter stage) {
        try {
            super.onStageInitialize(stage);
            // 设置上次保存的页面大小
            if (this.setting.isRememberPageSize()) {
                if (this.setting.isPageMaximized()) {
                    this.stage.setMaximized(true);
                    if (JulLog.isDebugEnabled()) {
                        JulLog.debug("view maximized");
                    }
                } else if (this.setting.getPageWidth() != null && this.setting.getPageHeight() != null) {
                    this.stage.setSize(this.setting.getPageWidth(), this.setting.getPageHeight());
                    if (JulLog.isDebugEnabled()) {
                        JulLog.debug("view width:{} height:{}", this.setting.getPageWidth(), this.setting.getPageHeight());
                    }
                }
            }
            // 设置上次保存的页面位置
            if (this.setting.isRememberPageLocation() && !this.setting.isPageMaximized() && this.setting.getPageScreenX() != null && this.setting.getPageScreenY() != null) {
                this.stage.setLocation(this.setting.getPageScreenX(), this.setting.getPageScreenY());
                if (JulLog.isDebugEnabled()) {
                    JulLog.debug("view x:{} y:{}", this.setting.getPageScreenX(), this.setting.getPageScreenY());
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JulLog.warn("onStageInitialize error", ex);
        }
    }

    @Override
    public String getViewTitle() {
        return I18nResourceBundle.i18nString("shell.title.main");
    }
}
