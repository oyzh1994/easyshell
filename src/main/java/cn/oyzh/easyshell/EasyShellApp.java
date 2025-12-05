package cn.oyzh.easyshell;

import cn.oyzh.common.SysConst;
import cn.oyzh.common.dto.Project;
import cn.oyzh.common.exception.ExceptionUtil;
import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.system.OSUtil;
import cn.oyzh.common.system.SystemUtil;
import cn.oyzh.easyshell.domain.ShellSetting;
import cn.oyzh.easyshell.exception.ShellExceptionParser;
import cn.oyzh.easyshell.internal.ShellClientChecker;
import cn.oyzh.easyshell.store.ShellSettingStore;
import cn.oyzh.easyshell.store.ShellStoreUtil;
import cn.oyzh.easyshell.tabs.message.ShellMessageTabController;
import cn.oyzh.easyshell.terminal.redis.RedisTerminalManager;
import cn.oyzh.easyshell.terminal.redis.RedisTerminalPane;
import cn.oyzh.easyshell.terminal.zk.ZKTerminalManager;
import cn.oyzh.easyshell.terminal.zk.ZKTerminalPane;
import cn.oyzh.easyshell.util.ShellViewFactory;
import cn.oyzh.easyshell.x11.ShellX11Manager;
import cn.oyzh.event.EventFactory;
import cn.oyzh.event.EventFormatter;
import cn.oyzh.event.EventListener;
import cn.oyzh.event.EventSubscribe;
import cn.oyzh.fx.gui.tray.DesktopTrayItem;
import cn.oyzh.fx.gui.tray.QuitTrayItem;
import cn.oyzh.fx.gui.tray.SettingTrayItem;
import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.fx.plus.event.FXEventBus;
import cn.oyzh.fx.plus.event.FXEventConfig;
import cn.oyzh.fx.plus.ext.FXApplication;
import cn.oyzh.fx.plus.font.FontManager;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.opacity.OpacityManager;
import cn.oyzh.fx.plus.theme.ThemeManager;
import cn.oyzh.fx.plus.tray.TrayManager;
import cn.oyzh.fx.plus.util.FXUtil;
import cn.oyzh.fx.plus.window.StageManager;
import cn.oyzh.fx.terminal.util.TerminalManager;
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
            // 开启fx的预览功能
            System.setProperty("javafx.enablePreview", "true");
            System.setProperty("javafx.suppressPreviewWarning", "true");
            // 设置默认异常捕捉器
            Thread.setDefaultUncaughtExceptionHandler((t, ex) -> {
                if (!ExceptionUtil.hasMessage(ex, "isImageAutoSize")) {
                    ex.printStackTrace();
                    JulLog.error("thread:{} caught error:{}", t.getName(), ex.getMessage());
                }
            });
            // // 关闭BouncyCastle的自签名检查
            // System.setProperty(PKCS1Encoding.NOT_STRICT_LENGTH_ENABLED_PROPERTY, "true");
            SysConst.projectName(PROJECT.getName());
            SysConst.storeDir(ShellConst.getStorePath());
            SysConst.cacheDir(ShellConst.getCachePath());
            if (JulLog.isInfoEnabled()) {
                JulLog.info("程序启动中...");
            }
            // 储存初始化
            ShellStoreUtil.init();
            if (OSUtil.isWindows()) {
                FXConst.appIcon(ShellConst.ICON_32_PATH);
            } else {
                FXConst.appIcon(ShellConst.ICON_PATH);
            }
            // 事件总线
            EventFactory.registerEventBus(FXEventBus.class);
            EventFactory.syncEventConfig(FXEventConfig.SYNC);
            EventFactory.asyncEventConfig(FXEventConfig.ASYNC);
            EventFactory.defaultEventConfig(FXEventConfig.DEFAULT);
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
            if (JulLog.isInfoEnabled()) {
                JulLog.info("{} init start.", SysConst.projectName());
            }
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
            // 开启定期gc
            SystemUtil.gcInterval(60_000);
            // 注册命令
            TerminalManager.setLoadHandler(ZKTerminalPane.TERMINAL_NAME, ZKTerminalManager::registerHandlers);
            TerminalManager.setLoadHandler(RedisTerminalPane.TERMINAL_NAME, RedisTerminalManager::registerHandlers);
        } catch (Exception ex) {
            ex.printStackTrace();
            JulLog.warn("start error", ex);
        }
    }

    @Override
    public void stop() {
        // 停止客户端检测
        ShellClientChecker.stop();
        // 关闭x11服务
        ShellX11Manager.stopXServer();
        // 储存销毁
        ShellStoreUtil.destroy();
        EventListener.super.unregister();
        super.stop();
    }

    @Override
    protected void showMainView() {
        // try {
        //     // 显示主页面
        //     StageManager.showStage(MainController.class);
        // } catch (Exception ex) {
        //     ex.printStackTrace();
        //     JulLog.warn("showMainView error", ex);
        // }
        ShellViewFactory.shellMain();
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
            TrayManager.addMenuItem(new DesktopTrayItem("12", ShellViewFactory::shellMain));
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
                    ShellViewFactory.shellMain();
                }
            });
            // 显示托盘
            TrayManager.show();
        } catch (Exception ex) {
            JulLog.warn("不支持系统托盘!", ex);
        }
    }

    /**
     * 事件消息
     *
     * @param formatter 事件
     */
    @EventSubscribe
    private void onEventMsg(EventFormatter formatter) {
        ShellMessageTabController.EVENT_MESSAGES.add(formatter);
    }
}
