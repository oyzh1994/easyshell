package cn.oyzh.easyshell;

import cn.oyzh.common.SysConst;
import cn.oyzh.common.dto.Project;
import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.system.OSUtil;
import cn.oyzh.easyshell.controller.AboutController;
import cn.oyzh.easyshell.controller.MainController;
import cn.oyzh.easyshell.controller.SettingController;
import cn.oyzh.easyshell.controller.connect.ShellAddConnectController;
import cn.oyzh.easyshell.controller.connect.ShellExportConnectController;
import cn.oyzh.easyshell.controller.connect.ShellImportConnectController;
import cn.oyzh.easyshell.controller.connect.ShellUpdateConnectController;
import cn.oyzh.easyshell.controller.sftp.ShellSftpFileInfoController;
import cn.oyzh.easyshell.controller.sftp.ShellSftpTransportController;
import cn.oyzh.easyshell.controller.tool.ShellToolController;
import cn.oyzh.easyshell.domain.ShellSetting;
import cn.oyzh.easyshell.event.serialPort.SerialPortNewEvent;
import cn.oyzh.easyshell.event.window.*;
import cn.oyzh.easyshell.exception.ShellExceptionParser;
import cn.oyzh.easyshell.store.ShellSettingStore;
import cn.oyzh.easyshell.store.ShellStoreUtil;
import cn.oyzh.easyshell.tabs.serialPort.SerialPortSettingController;
import cn.oyzh.easyshell.x11.X11Manager;
import cn.oyzh.event.EventFactory;
import cn.oyzh.event.EventListener;
import cn.oyzh.event.EventSubscribe;
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
import cn.oyzh.fx.plus.window.StageAdapter;
import cn.oyzh.fx.plus.window.StageManager;
import cn.oyzh.i18n.I18nManager;
import javafx.application.Platform;
import javafx.stage.Stage;

import java.awt.event.MouseEvent;
import java.util.List;


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
            // 抗锯齿优化
            System.setProperty("prism.text", "t2k");
            System.setProperty("prism.lcdtext", "false");
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
        super.stop();
        // 关闭x11服务
        X11Manager.stopXServer();
        EventListener.super.unregister();
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
            TrayManager.addMenuItem(new DesktopTrayItem("12", this::showMain));
            // 打开设置
            TrayManager.addMenuItem(new SettingTrayItem("12", () -> this.showSetting(null)));
            // 退出程序
            TrayManager.addMenuItem(new QuitTrayItem("12", () -> {
                JulLog.warn("exit app by tray.");
                StageManager.exit();
            }));
            // 鼠标事件
            TrayManager.onMouseClicked(e -> {
                // 单击鼠标主键，显示主页
                if (e.getButton() == MouseEvent.BUTTON1) {
                    this.showMain();
                }
            });
            // 显示托盘
            TrayManager.show();
        } catch (Exception ex) {
            JulLog.warn("不支持系统托盘!", ex);
        }
    }

    /**
     * 显示主页
     */
    private void showMain() {
        FXUtil.runLater(() -> {
            try {
                StageAdapter adapter = StageManager.getStage(MainController.class);
                if (adapter != null) {
                    JulLog.info("front main.");
                    adapter.toFront();
                } else {
                    JulLog.info("show main.");
                    StageManager.showStage(MainController.class);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                MessageBox.exception(ex);
            }
        });
    }

    /**
     * 显示设置
     */
    @EventSubscribe
    private void showSetting(ShellShowSettingEvent event) {
        FXUtil.runLater(() -> {
            try {

                StageAdapter adapter = StageManager.getStage(SettingController.class);
                if (adapter != null) {
                    JulLog.info("front setting.");
                    adapter.toFront();
                } else {
                    JulLog.info("show setting.");
                    StageManager.showStage(SettingController.class, StageManager.getPrimaryStage());
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                MessageBox.exception(ex);
            }
        });
    }

    /**
     * 显示添加连接
     */
    @EventSubscribe
    private void addConnect(ShellShowAddConnectEvent event) {
        FXUtil.runLater(() -> {
            try {
                StageAdapter adapter = StageManager.parseStage(ShellAddConnectController.class);
                adapter.setProp("group", event.data());
                adapter.display();
            } catch (Exception ex) {
                ex.printStackTrace();
                MessageBox.exception(ex);
            }
        });
    }

    /**
     * 显示修改连接
     */
    @EventSubscribe
    private void updateConnect(ShellShowUpdateConnectEvent event) {
        FXUtil.runLater(() -> {
            try {
                StageAdapter adapter = StageManager.parseStage(ShellUpdateConnectController.class);
                adapter.setProp("shellConnect", event.data());
                adapter.display();
            } catch (Exception ex) {
                ex.printStackTrace();
                MessageBox.exception(ex);
            }
        });
    }

    /**
     * 显示工具页面
     */
    @EventSubscribe
    private void tool(ShellShowToolEvent event) {
        FXUtil.runLater(() -> {
            try {
                StageManager.showStage(ShellToolController.class, StageManager.getPrimaryStage());
            } catch (Exception ex) {
                ex.printStackTrace();
                MessageBox.exception(ex);
            }
        });
    }

    /**
     * 显示关于页面
     */
    @EventSubscribe
    private void about(ShellShowAboutEvent event) {
        FXUtil.runLater(() -> {
            try {
                StageManager.showStage(AboutController.class, StageManager.getPrimaryStage());
            } catch (Exception ex) {
                ex.printStackTrace();
                MessageBox.exception(ex);
            }
        });
    }

    /**
     * 显示导出连接页面
     */
    @EventSubscribe
    private void exportConnect(ShellShowExportConnectEvent event) {
        FXUtil.runLater(() -> {
            try {
                StageManager.showStage(ShellExportConnectController.class, StageManager.getPrimaryStage());
            } catch (Exception ex) {
                ex.printStackTrace();
                MessageBox.exception(ex);
            }
        });
    }

    /**
     * 显示导入连接页面
     */
    @EventSubscribe
    private void importConnect(ShellShowImportConnectEvent event) {
        FXUtil.runLater(() -> {
            try {
                StageAdapter adapter = StageManager.parseStage(ShellImportConnectController.class, StageManager.getPrimaryStage());
                adapter.setProp("file", event.data());
                adapter.display();
            } catch (Exception ex) {
                ex.printStackTrace();
                MessageBox.exception(ex);
            }
        });
    }

    /**
     * 显示文件信息页面
     */
    @EventSubscribe
    private void fileInfo(ShellShowFileInfoEvent event) {
        FXUtil.runLater(() -> {
            try {
                StageAdapter adapter = StageManager.parseStage(ShellSftpFileInfoController.class, StageManager.getPrimaryStage());
                adapter.setProp("file", event.data());
                adapter.display();
            } catch (Exception ex) {
                ex.printStackTrace();
                MessageBox.exception(ex);
            }
        });
    }

    /**
     * 显示传输数据
     */
    @EventSubscribe
    private void transportData(ShellShowTransportFileEvent event) {
        // 判断窗口是否存在
        List<StageAdapter> list = StageManager.listStage(ShellSftpTransportController.class);
        for (StageAdapter adapter : list) {
            if (adapter.getProp("sourceConnect") == event.data()) {
                adapter.toFront();
                return;
            }
        }
        FXUtil.runLater(() -> {
            try {
                StageAdapter adapter = StageManager.parseStage(ShellSftpTransportController.class, null);
                adapter.setProp("sourceConnect", event.data());
                adapter.display();
            } catch (Exception ex) {
                ex.printStackTrace();
                MessageBox.exception(ex);
            }
        });
    }

    @EventSubscribe
    public void showSerialPortWindow(SerialPortNewEvent ignoredEvent) {
        Platform.runLater(() -> {
            try {
                StageManager.showStage(SerialPortSettingController.class, StageManager.getPrimaryStage());
            } catch (Exception e) {
                JulLog.error("Failed to show SerialPortWindow", e);
                MessageBox.exception(e);
            }
        });
    }
}
