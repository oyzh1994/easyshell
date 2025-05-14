package cn.oyzh.easyshell;

import cn.oyzh.common.SysConst;
import cn.oyzh.common.dto.Project;
import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.system.OSUtil;
import cn.oyzh.easyshell.controller.MainController;
import cn.oyzh.easyshell.domain.ShellSetting;
import cn.oyzh.easyshell.exception.ShellExceptionParser;
import cn.oyzh.easyshell.store.ShellSettingStore;
import cn.oyzh.easyshell.store.ShellStoreUtil;
import cn.oyzh.easyshell.util.ShellViewFactory;
import cn.oyzh.easyshell.x11.ShellX11Manager;
import cn.oyzh.event.EventFactory;
import cn.oyzh.event.EventListener;
import cn.oyzh.fx.gui.tray.DesktopTrayItem;
import cn.oyzh.fx.gui.tray.QuitTrayItem;
import cn.oyzh.fx.gui.tray.SettingTrayItem;
import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.fx.plus.event.FxEventBus;
import cn.oyzh.fx.plus.event.FxEventConfig;
import cn.oyzh.fx.plus.ext.FXApplication;
import cn.oyzh.fx.plus.font.FontManager;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.opacity.OpacityManager;
import cn.oyzh.fx.plus.theme.ThemeManager;
import cn.oyzh.fx.plus.tray.TrayManager;
import cn.oyzh.fx.plus.util.FXUtil;
import cn.oyzh.fx.plus.window.StageManager;
import cn.oyzh.i18n.I18nManager;
import javafx.stage.Stage;

import java.awt.event.MouseEvent;


/**
 * 程序主入口
 *
 * @author oyzh
 * @since 2023/08/16
 */
public class EasyShellApp extends FXApplication implements EventListener {

    /**
     * 项目信息
     */
    private static final Project PROJECT = Project.load();

    public static void main(String[] args) {
        try {
            // 渲染模式
//            System.setProperty("prism.order", "es2");
            // 垂直同步
//            System.setProperty("prism.vsync", "false");
            // 路径缓存大小
//            System.setProperty("prism.glyphCacheWidth", "1024");
//            System.setProperty("prism.glyphCacheHeight", "1024");
//            // 调试模式
//            System.setProperty("prism.debug", "true");
//            // 详细模式
//            System.setProperty("javafx.verbose", "true");
            // 打印分配
//            System.setProperty("prism.printallocs", "true");
//            // 图形缓存
//            System.setProperty("prism.cache", "false");
//            System.setProperty("prism.cacheshapes", "false");
            // 脏区域重绘
//            System.setProperty("prism.dirtyopts", "false");
            // 纹理尺寸
//            System.setProperty("prism.maxTextureSize", "1024");
            // 禁用区域缓存
//            System.setProperty("prism.disableRegionCaching", "true");
            // 抗锯齿优化
//            System.setProperty("prism.text", "t2k");
//            System.setProperty("prism.lcdtext", "false");
            SysConst.projectName(PROJECT.getName());
            SysConst.storeDir(ShellConst.getStorePath());
            SysConst.cacheDir(ShellConst.getCachePath());
            JulLog.info("项目启动中...");
            // 储存初始化
            ShellStoreUtil.init();
            if (OSUtil.isWindows()) {
                FXConst.appIcon(ShellConst.ICON_32_PATH);
            } else {
                FXConst.appIcon(ShellConst.ICON_PATH);
            }
            // 事件总线
            EventFactory.registerEventBus(FxEventBus.class);
            EventFactory.syncEventConfig(FxEventConfig.SYNC);
            EventFactory.asyncEventConfig(FxEventConfig.ASYNC);
            EventFactory.defaultEventConfig(FxEventConfig.DEFAULT);
            launch(EasyShellApp.class, args);
        } catch (Exception ex) {
            ex.printStackTrace();
            JulLog.warn("main error", ex);
        }
    }

    @Override
    public void init() throws Exception {
        try {
            // fx程序实例
            FXConst.INSTANCE = this;
            // 日志开始
            JulLog.info("{} init start.", SysConst.projectName());
            // 禁用fx的css日志
            FXUtil.disableCSSLogger();
            // 配置对象
            ShellSetting setting = ShellSettingStore.SETTING;
            // 应用区域
            I18nManager.apply(setting.getLocale());
            // 应用字体
            FontManager.apply(setting.fontConfig());
            // 应用主题
            ThemeManager.apply(setting.themeConfig());
            // 应用透明度
            OpacityManager.apply(setting.opacityConfig());
            // 注册异常处理器
            MessageBox.registerExceptionParser(ShellExceptionParser.INSTANCE);
            // 注册事件处理
            EventListener.super.register();
            // 调用父类
            super.init();
        } catch (Exception ex) {
            ex.printStackTrace();
            JulLog.warn("main error", ex);
        }
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            super.start(primaryStage);
        } catch (Exception ex) {
            ex.printStackTrace();
            JulLog.warn("start error", ex);
        }
    }

    @Override
    public void stop() {
        // 关闭x11服务
        ShellX11Manager.stopXServer();
        EventListener.super.unregister();
        super.stop();
    }

    @Override
    protected void showMainView() {
        try {
            // 显示主页面
            StageManager.showStage(MainController.class);
        } catch (Exception ex) {
            ex.printStackTrace();
            JulLog.warn("showMainView error", ex);
        }
    }

    @Override
    protected void initSystemTray() {
        try {
            if (!TrayManager.supported()) {
                JulLog.warn("tray is not supported.");
                return;
            }
            if (TrayManager.exist()) {
                return;
            }
            // 初始化
            if (OSUtil.isWindows()) {
                TrayManager.init(ShellConst.ICON_24_PATH);
            } else {
                TrayManager.init(ShellConst.ICON_PATH);
            }
            // 设置标题
            TrayManager.setTitle(PROJECT.getName() + " v" + PROJECT.getVersion());
            // 打开主页
            TrayManager.addMenuItem(new DesktopTrayItem("12", ShellViewFactory::main));
            // 打开设置
            TrayManager.addMenuItem(new SettingTrayItem("12", ShellViewFactory::setting));
            // 退出程序
            TrayManager.addMenuItem(new QuitTrayItem("12", () -> {
                JulLog.warn("exit app by tray.");
                StageManager.exit();
            }));
            // 鼠标事件
            TrayManager.onMouseClicked(e -> {
                // 单击鼠标主键，显示主页
                if (e.getButton() == MouseEvent.BUTTON1) {
                    ShellViewFactory.main();
                }
            });
            // 显示托盘
            TrayManager.show();
        } catch (Exception ex) {
            JulLog.warn("不支持系统托盘!", ex);
        }
    }






}
