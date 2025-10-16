package cn.oyzh.easyshell.tabs.ssh;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.thread.ExecutorUtil;
import cn.oyzh.common.thread.TaskManager;
import cn.oyzh.common.thread.ThreadUtil;
import cn.oyzh.common.util.NumberUtil;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.ssh2.ShellSSHClient;
import cn.oyzh.easyshell.ssh2.ShellSSHTermWidget;
import cn.oyzh.easyshell.ssh2.ShellSSHTtyConnector;
import cn.oyzh.easyshell.ssh2.server.ShellServerExec;
import cn.oyzh.easyshell.ssh2.server.ShellServerMonitor;
import cn.oyzh.easyshell.tabs.ShellSnippetAdapter;
import cn.oyzh.easyshell.util.ShellConnectUtil;
import cn.oyzh.easyshell.util.ShellViewFactory;
import cn.oyzh.fx.gui.tabs.SubTabController;
import cn.oyzh.fx.plus.controls.label.FXLabel;
import cn.oyzh.fx.plus.controls.tab.FXTab;
import cn.oyzh.fx.plus.controls.toggle.FXToggleSwitch;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.i18n.I18nHelper;
import com.jediterm.terminal.TtyConnector;
import com.jediterm.terminal.ui.FXTerminalPanel;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.concurrent.Future;

/**
 * ssh-终端tab内容组件
 *
 * @author oyzh
 * @since 2025/03/11
 */
public class ShellSSHTermTabController extends SubTabController implements ShellSnippetAdapter {

    // /**
    //  * 终端容器
    //  */
    // @FXML
    // private FXVBox widgetBox;

    /**
     * 终端组件
     */
    @FXML
    private ShellSSHTermWidget widget;

    /**
     * 服务监控信息
     */
    @FXML
    private FXLabel serverMonitorInfo;

    /**
     * 监控任务
     */
    private Future<?> serverMonitorTask;

    /**
     * 服务监控
     */
    @FXML
    private FXToggleSwitch serverMonitor;

    /**
     * 终端大小
     */
    @FXML
    private FXLabel termSize;

    // /**
    //  * 设置
    //  */
    // private final ShellSetting setting = ShellSettingStore.SETTING;
    //
    // /**
    //  * 设置存储
    //  */
    // private final ShellSettingStore settingStore = ShellSettingStore.INSTANCE;

    /**
     * 初始化组件
     *
     * @throws IOException 异常
     */
    private void initWidget() throws Exception {
        // // 关闭和移除旧的组件
        // if (this.widget != null) {
        //     this.widget.close();
        //     this.widgetBox.removeChild(this.widget);
        //     TtyConnector connector = this.widget.getTtyConnector();
        //     if (connector != null) {
        //         connector.close();
        //     }
        // }
        // // 已关闭
        // ShellSSHClient client = this.client();
        // ChannelShell shell = client.getShell();
        // // 已关闭
        // if (shell == null) {
        //     this.closeTab();
        //     return;
        // }
        // // 初始化组件
        // this.widget = new ShellSSHTermWidget();
        // this.widget.setFlexWidth("100%");
        // this.widget.setFlexHeight("100% - 30");
        // this.widgetBox.addChild(0, this.widget);
        // 初始化退格码
        this.widget.initBackspaceCode(this.shellConnect().getBackspaceType());
        this.widget.openSession(this.initTtyConnector());
        // // 获取焦点
        // FXUtil.runLater(this.widget::requestFocus);
    }

    /**
     * 初始化tty连接器
     *
     * @return tty连接器
     * @throws IOException 异常
     */
    private TtyConnector initTtyConnector() throws Exception {
        ShellSSHClient client = this.client();
        ShellConnect connect = client.getShellConnect();
        Charset charset = client.getCharset();
        TtyConnector ttyConnector;
        ShellSSHTtyConnector connector = this.widget.createTtyConnector(charset);
        // 监听窗口大小
        connector.terminalSizeProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                this.termSize.text(newValue.getRows() + "x" + newValue.getColumns());
            }
        });
        if (connect.isEnableZModem()) {
            // connector.setResetTtyConnectorCallback(this::openShell);
            ttyConnector = this.widget.createZModemTtyConnector(connector);
        } else {
            ttyConnector = connector;
        }
        connector.init(client);
        return ttyConnector;
    }

    // /**
    //  * 打开shell
    //  *
    //  * @return 结果
    //  */
    // private boolean openShell() {
    //     DownLatch latch = DownLatch.of();
    //     ShellSSHClient client = this.client();
    //     AtomicReference<Exception> ref = new AtomicReference<>();
    //     ThreadUtil.start(() -> {
    //         try {
    //             ChannelShell shell = client.reopenShell();
    //             this.initWidget();
    //             // shell.connect(client.connectTimeout());
    //         } catch (Exception ex) {
    //             ref.set(ex);
    //         } finally {
    //             latch.countDown();
    //         }
    //     });
    //     latch.await();
    //     if (ref.get() != null) {
    //         MessageBox.exception(ref.get());
    //         this.closeTab();
    //         return false;
    //     }
    //     return true;
    // }

    /**
     * 初始化背景
     */
    private void initBackground() {
        ShellConnect connect = this.client().getShellConnect();
        FXTerminalPanel terminalPanel = this.widget.getTerminalPanel();
        // 处理背景
        ShellConnectUtil.initBackground(connect, terminalPanel);
    }

    /**
     * 初始化
     *
     * @throws Exception 异常
     */
    public void init() throws Exception {
        // ShellSSHClient client = this.client();
        // ChannelShell shell = client.openShell();
        this.initWidget();
        // shell.open().verify(client.connectTimeout());
        // if (!this.openShell()) {
        //     return;
        // }
        // shell.connect(client.connectTimeout());
        // if (!shell.isConnected()) {
        //     MessageBox.warn(I18nHelper.connectFail());
        //     this.closeTab();
        //     return;
        // }
        // 异步加载背景
        ThreadUtil.startVirtual(this::initBackground);
        // 初始化
        this.serverMonitor.setSelected(this.shellConnect().isServerMonitor());
        // this.serverMonitor.setSelected(this.setting.isSshServerMonitor());
    }

    @Override
    public void onTabInit(FXTab tab) {
        super.onTabInit(tab);
        // 服务监控
        this.serverMonitor.selectedChanged((observable, oldValue, newValue) -> {
            if (newValue) {
                this.initMonitorTask();
            } else {
                this.closeMonitorTask();
            }
            // 存储
            this.shellConnect().setServerMonitor(newValue);
            // this.setting.setSshServerMonitor(newValue);
            // this.settingStore.update(this.setting);
        });
    }

    @Override
    public void onTabClosed(Event event) {
        super.onTabClosed(event);
        this.widget.close();
    }

    @Override
    public ShellSSHTabController parent() {
        return (ShellSSHTabController) super.parent();
    }

    public ShellSSHClient client() {
        return this.parent().getClient();
    }

    public ShellConnect shellConnect() {
        return this.parent().shellConnect();
    }

    /**
     * 初始化监控任务
     */
    private void initMonitorTask() {
        // 处理组件
        this.serverMonitorInfo.display();
        if (this.serverMonitorTask != null) {
            return;
        }
        try {
            ShellServerExec serverExec = this.client().serverExec();
            this.serverMonitorTask = TaskManager.startInterval(() -> {
                // 任务已取消
                if (serverExec.getClient() == null) {
                    ExecutorUtil.cancel(this.serverMonitorTask);
                    return;
                }
                // 获取数据
                ShellServerMonitor monitor = serverExec.monitor();
                if (monitor == null) {
                    return;
                }
                StringBuilder sb = new StringBuilder();
                double cpuUsage = NumberUtil.scale(monitor.getCpuUsage(), 2);
                double memoryUsage = NumberUtil.scale(monitor.getMemoryUsage(), 2);
                double networkRecv = NumberUtil.scale(monitor.getNetworkReceiveSpeed(), 2);
                double networkSend = NumberUtil.scale(monitor.getNetworkSendSpeed(), 2);
                double diskRead = NumberUtil.scale(monitor.getDiskReadSpeed(), 2);
                double diskWrite = NumberUtil.scale(monitor.getDiskWriteSpeed(), 2);
                sb.append("CPU:").append(cpuUsage).append("% | ");
                sb.append(I18nHelper.memory()).append(":").append(memoryUsage).append("% | ");
                sb.append(I18nHelper.networkInput()).append(":").append(networkRecv).append("KB/s | ");
                sb.append(I18nHelper.networkOutput()).append(":").append(networkSend).append("KB/s | ");
                sb.append(I18nHelper.diskRead()).append(":").append(diskRead).append("MB/s | ");
                sb.append(I18nHelper.diskWrite()).append(":").append(diskWrite).append("MB/s");
                this.serverMonitorInfo.text(sb.toString());
            }, 3_000, 0);
            if (JulLog.isDebugEnabled()) {
                JulLog.debug("MonitorTask started.");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JulLog.error("init MonitorTask error", ex);
        }
    }

    /**
     * 关闭监控任务
     */
    public void closeMonitorTask() {
        try {
            ExecutorUtil.cancel(this.serverMonitorTask);
            this.serverMonitorTask = null;
            this.serverMonitorInfo.clear();
            this.serverMonitorInfo.disappear();
            if (JulLog.isDebugEnabled()) {
                JulLog.debug("MonitorTask closed.");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            JulLog.error("close MonitorTask error", ex);
        }
    }

    /**
     * 终端历史
     *
     * @param event 事件
     */
    @FXML
    private void termHistory(MouseEvent event) {
        ShellViewFactory.termHistory((Node) event.getSource(), this.client(), h -> {
            try {
                this.widget.getTtyConnector().writeLine(h);
            } catch (Exception ex) {
                MessageBox.exception(ex);
            }
        });
    }

    /**
     * 片段列表
     *
     * @param event 事件
     */
    @FXML
    private void snippet(MouseEvent event) {
        ShellSnippetAdapter.super.snippetList((Node) event.getSource());
    }

    @Override
    public void runSnippet(String content) throws IOException {
        this.widget.getTtyConnector().write(content);
    }
}
