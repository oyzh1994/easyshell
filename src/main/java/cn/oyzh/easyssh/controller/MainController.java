package cn.oyzh.easyssh.controller;

import cn.oyzh.common.dto.Project;
import cn.oyzh.easyfx.controller.FXController;
import cn.oyzh.easyfx.event.EventReceiver;
import cn.oyzh.easyfx.event.EventUtil;
import cn.oyzh.easyfx.information.MessageBox;
import cn.oyzh.easyfx.svg.SVGGlyph;
import cn.oyzh.easyfx.tray.FXSystemTray;
import cn.oyzh.easyfx.util.FXUtil;
import cn.oyzh.easyfx.view.FXView;
import cn.oyzh.easyfx.view.FXViewUtil;
import cn.oyzh.easyfx.view.FXWindow;
import cn.oyzh.easyssh.SSHConst;
import cn.oyzh.easyssh.SSHStyle;
import cn.oyzh.easyssh.domain.SSHPageInfo;
import cn.oyzh.easyssh.domain.SSHSetting;
import cn.oyzh.easyssh.ssh.SSHEvents;
import cn.oyzh.easyssh.store.PageInfoStore;
import cn.oyzh.easyssh.store.SSHSettingStore;
import cn.oyzh.fx.plus.controller.StageController;
import cn.oyzh.fx.plus.window.StageAttribute;
import javafx.fxml.FXML;
import javafx.stage.WindowEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.awt.event.MouseEvent;
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
        title = "EasySSH主页",
        value = SSHConst.FXML_BASE_PATH + "main.fxml"
)
public class MainController extends StageController {

    /**
     * 项目信息
     */
    private Project project;
//
//    /**
//     * 系统托盘
//     */
//    private static FXSystemTray tray;

    /**
     * 头部页面
     */
    @FXML
    private HeaderController headerController;

    /**
     * ssh主页业务
     */
    @FXML
    private SSHMainController sshMainController;

    /**
     * 页面信息
     */
    private final SSHPageInfo pageInfo = PageInfoStore.PAGE_INFO;

    /**
     * ssh相关配置
     */
    private final SSHSetting setting = SSHSettingStore.SETTING;

    /**
     * 页面信息储存
     */
    private final PageInfoStore pageInfoStore = PageInfoStore.INSTANCE;

//    /**
//     * 初始化系统托盘
//     */
//    private void initSystemTray() {
//        if (tray != null) {
//            return;
//        }
//        try {
//            // 初始化托盘
//            tray = new FXSystemTray(SSHConst.ICON_PATH);
//            // 设置标题
//            tray.setTitle(this.project.getName() + " v" + this.project.getVersion());
//            // 打开主页
//            tray.addMenuItem("打开", new SVGGlyph("/font/desktop.svg", "12"), this::showMain);
//            // 打开设置
//            tray.addMenuItem("设置", new SVGGlyph("/font/setting.svg", "12"), this::showSetting);
//            // 退出程序
//            tray.addMenuItem("退出", new SVGGlyph("/font/poweroff.svg", "12"), () -> {
//                JulLog.warn("exit app by tray.");
//                this.exit();
//            });
//            // 鼠标事件
//            tray.onMouseClicked(e -> {
//                // 单击鼠标主键，显示主页
//                if (e.getButton() == MouseEvent.BUTTON1) {
//                    this.showMain();
//                }
//            });
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
//    }

//    /**
//     * 显示设置
//     */
//    private void showSetting() {
//        FXUtil.runLater(() -> {
//            FXView fxView = FXViewUtil.getView(SettingController.class);
//            if (fxView != null) {
//                JulLog.info("front setting.");
//                fxView.toFront();
//            } else {
//                JulLog.info("show setting.");
//                FXViewUtil.showView(SettingController.class, this.view);
//            }
//        });
//    }
//
//    /**
//     * 显示主页
//     */
//    private void showMain() {
//        FXUtil.runLater(() -> {
//            FXView fxView = FXViewUtil.getView(MainController.class);
//            if (fxView != null) {
//                JulLog.info("front main.");
//                fxView.toFront();
//            } else {
//                JulLog.info("show main.");
//                FXViewUtil.showView(MainController.class);
//            }
//        });
//    }

    @Override
    public List<FXController> getSubControllers() {
        return Arrays.asList(this.sshMainController, this.headerController);
    }

    @Override
    public void onViewCloseRequest(WindowEvent event) {
        JulLog.warn("main view closing.");
        // 直接退出应用
        if (this.setting.isExitDirectly()) {
            JulLog.info("exit directly.");
            this.exit();
            return;
        }

        // 总是询问
        if (this.setting.isExitAsk()) {
            if (MessageBox.confirm("确定退出" + this.project.getName() + "？")) {
                JulLog.info("exit by confirm.");
                this.exit();
            } else {
                JulLog.info("cancel by confirm.");
                event.consume();
            }
            return;
        }

        // 系统托盘
        if (this.setting.isExitTray()) {
            if (tray != null) {
                JulLog.info("show tray.");
                tray.show();
            } else {
                JulLog.error("tray not support!");
                MessageBox.warn("不支持系统托盘！");
            }
        }
    }

    @Override
    public void onWindowShowing(WindowEvent event) {
        super.onWindowShowing(event);
        this.view.setTitle(this.project.getName() + "-v" + this.project.getVersion());
    }

    @Override
    public void onWindowHidden(WindowEvent event) {
        super.onWindowHidden(event);
        // 取消注册事件处理
        EventUtil.unregister(this);
    }

    @Override
    public void onWindowShown(WindowEvent event) {
        super.onWindowShown(event);
        // 注册事件处理
        EventUtil.register(this);
        try {
            this.initSystemTray();
            tray.show();
        } catch (Exception ex) {
            JulLog.warn("不支持系统托盘!");
            ex.printStackTrace();
        }
    }

    /**
     * 应用退出
     */
    @EventReceiver(SSHEvents.APP_EXIT)
    public void exit() {
        FXViewUtil.exit();
    }

    @Override
    public void onSystemExit() {
        boolean savePageInfo = false;
        // 记住页面大小
        if (this.setting.isRememberPageSize()) {
            this.pageInfo.setWidth(this.view.getWidth());
            this.pageInfo.setHeight(this.view.getHeight());
            this.pageInfo.setMaximized(this.view.isMaximized());
            savePageInfo = true;
        }
        // 记住页面位置
        if (this.setting.isRememberPageLocation()) {
            this.pageInfo.setScreenX(this.view.getX());
            this.pageInfo.setScreenY(this.view.getY());
            savePageInfo = true;
        }
        // 保存页面信息
        if (savePageInfo) {
            this.pageInfoStore.update(this.pageInfo);
        }

        // 关闭托盘
        if (tray != null) {
            tray.close();
        }
        super.onSystemExit();
    }

    @Override
    public void onViewInitialize(FXView view) {
        super.onViewInitialize(view);
        // 设置上次保存的页面大小
        if (this.setting.isRememberPageSize()) {
            if (this.pageInfo.isMaximized()) {
                this.view.setMaximized(true);
                if (JulLog.isDebugEnabled()) {
                    JulLog.debug("view setMaximized");
                }
            } else if (this.pageInfo.getWidth() != null && this.pageInfo.getHeight() != null) {
                this.view.setWidth(this.pageInfo.getWidth());
                this.view.setHeight(this.pageInfo.getHeight());
                if (JulLog.isDebugEnabled()) {
                    JulLog.debug("view setWidth:{} setHeight:{}", this.pageInfo.getWidth(), this.pageInfo.getHeight());
                }
            }
        }
        // 设置上次保存的页面位置
        if (this.setting.isRememberPageLocation() && !this.pageInfo.isMaximized() && this.pageInfo.getScreenX() != null && this.pageInfo.getScreenY() != null) {
            this.view.setX(this.pageInfo.getScreenX());
            this.view.setY(this.pageInfo.getScreenY());
            if (JulLog.isDebugEnabled()) {
                JulLog.debug("view setX:{} setY:{}", this.pageInfo.getScreenX(), this.pageInfo.getScreenY());
            }
        }
    }
}
