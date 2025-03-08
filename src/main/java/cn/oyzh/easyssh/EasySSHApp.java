package cn.oyzh.easyssh;

import cn.oyzh.common.SysConst;
import cn.oyzh.common.dto.Project;
import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.system.OSUtil;
import cn.oyzh.easyssh.controller.AboutController;
import cn.oyzh.easyssh.controller.MainController;
import cn.oyzh.easyssh.controller.SettingController;
import cn.oyzh.easyssh.controller.connect.SSHAddConnectController;
import cn.oyzh.easyssh.controller.connect.SSHExportConnectController;
import cn.oyzh.easyssh.controller.connect.SSHImportConnectController;
import cn.oyzh.easyssh.controller.connect.SSHUpdateConnectController;
import cn.oyzh.easyssh.controller.sftp.SSHSftpFileInfoController;
import cn.oyzh.easyssh.controller.tool.SSHToolController;
import cn.oyzh.easyssh.domain.SSHSetting;
import cn.oyzh.easyssh.event.window.SSHShowAboutEvent;
import cn.oyzh.easyssh.event.window.SSHShowAddConnectEvent;
import cn.oyzh.easyssh.event.window.SSHShowExportConnectEvent;
import cn.oyzh.easyssh.event.window.SSHShowFileInfoEvent;
import cn.oyzh.easyssh.event.window.SSHShowImportConnectEvent;
import cn.oyzh.easyssh.event.window.SSHShowSettingEvent;
import cn.oyzh.easyssh.event.window.SSHShowToolEvent;
import cn.oyzh.easyssh.event.window.SSHShowUpdateConnectEvent;
import cn.oyzh.easyssh.parser.SSHExceptionParser;
import cn.oyzh.easyssh.store.SSHSettingStore;
import cn.oyzh.easyssh.store.SSHStoreUtil;
import cn.oyzh.easyssh.x11.X11Manager;
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
import javafx.stage.Stage;

import java.awt.event.MouseEvent;


/**
 * 程序主入口
 *
 * @author oyzh
 * @since 2023/08/16
 */
public class EasySSHApp extends FXApplication implements EventListener {

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
            SysConst.storeDir(SSHConst.STORE_PATH);
            JulLog.info("项目启动中...");
            // 储存初始化
            SSHStoreUtil.init();
            SysConst.storeDir(SSHConst.STORE_PATH);
            SysConst.cacheDir(SSHConst.CACHE_PATH);
            FXConst.appIcon(SSHConst.ICON_PATH);
            // 事件总线
            EventFactory.registerEventBus(FxEventBus.class);
            EventFactory.syncEventConfig(FxEventConfig.SYNC);
            EventFactory.asyncEventConfig(FxEventConfig.ASYNC);
            EventFactory.defaultEventConfig(FxEventConfig.DEFAULT);
            launch(EasySSHApp.class, args);
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
            SSHSetting setting = SSHSettingStore.SETTING;
            // 应用区域
            I18nManager.apply(setting.getLocale());
            // 应用字体
            FontManager.apply(setting.fontConfig());
            // 应用主题
            ThemeManager.apply(setting.themeConfig());
            // 应用透明度
            OpacityManager.apply(setting.opacityConfig());
            // 注册异常处理器
            MessageBox.registerExceptionParser(SSHExceptionParser.INSTANCE);
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
                TrayManager.init(SSHConst.TRAY_ICON_PATH);
            } else {
                TrayManager.init(SSHConst.ICON_PATH);
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
    private void showSetting(SSHShowSettingEvent event) {
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
    private void addConnect(SSHShowAddConnectEvent event) {
        FXUtil.runLater(() -> {
            try {
                StageAdapter adapter = StageManager.parseStage(SSHAddConnectController.class);
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
    private void updateConnect(SSHShowUpdateConnectEvent event) {
        FXUtil.runLater(() -> {
            try {
                StageAdapter adapter = StageManager.parseStage(SSHUpdateConnectController.class);
                adapter.setProp("sshConnect", event.data());
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
    private void tool(SSHShowToolEvent event) {
        FXUtil.runLater(() -> {
            try {
                StageManager.showStage(SSHToolController.class, StageManager.getPrimaryStage());
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
    private void about(SSHShowAboutEvent event) {
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
    private void exportConnect(SSHShowExportConnectEvent event) {
        FXUtil.runLater(() -> {
            try {
                StageManager.showStage(SSHExportConnectController.class, StageManager.getPrimaryStage());
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
    private void importConnect(SSHShowImportConnectEvent event) {
        FXUtil.runLater(() -> {
            try {
                StageAdapter adapter = StageManager.parseStage(SSHImportConnectController.class, StageManager.getPrimaryStage());
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
    private void fileInfo(SSHShowFileInfoEvent event) {
        FXUtil.runLater(() -> {
            try {
                StageAdapter adapter = StageManager.parseStage(SSHSftpFileInfoController.class, StageManager.getPrimaryStage());
                adapter.setProp("file", event.data());
                adapter.display();
            } catch (Exception ex) {
                ex.printStackTrace();
                MessageBox.exception(ex);
            }
        });
    }
}
