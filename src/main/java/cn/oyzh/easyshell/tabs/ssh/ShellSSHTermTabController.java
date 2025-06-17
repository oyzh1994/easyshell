package cn.oyzh.easyshell.tabs.ssh;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.thread.ExecutorUtil;
import cn.oyzh.common.thread.TaskManager;
import cn.oyzh.common.thread.ThreadUtil;
import cn.oyzh.common.util.NumberUtil;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.ssh.ShellSSHClient;
import cn.oyzh.easyshell.ssh.ShellSSHShell;
import cn.oyzh.easyshell.ssh.ShellSSHTermWidget;
import cn.oyzh.easyshell.ssh.ShellSSHTtyConnector;
import cn.oyzh.easyshell.ssh.server.ShellServerExec;
import cn.oyzh.easyshell.ssh.server.ShellServerMonitor;
import cn.oyzh.easyshell.util.ShellConnectUtil;
import cn.oyzh.easyshell.util.ShellViewFactory;
import cn.oyzh.easyshell.zmodem.ZModemPtyConnectorAdaptor;
import cn.oyzh.fx.gui.tabs.RichTab;
import cn.oyzh.fx.gui.tabs.SubTabController;
import cn.oyzh.fx.plus.controls.label.FXLabel;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.fx.plus.controls.toggle.FXToggleSwitch;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.i18n.I18nHelper;
import com.jediterm.terminal.ui.FXTerminalPanel;
import javafx.event.Event;
import javafx.fxml.FXML;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.concurrent.Future;

/**
 * ssh-终端tab内容组件
 *
 * @author oyzh
 * @since 2025/03/11
 */
public class ShellSSHTermTabController extends SubTabController {

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
     * 终端历史
     */
    @FXML
    private SVGGlyph termHistory;

    /**
     * 终端大小
     */
    @FXML
    private FXLabel termSize;

    /**
     * 初始化组件
     *
     * @throws IOException 异常
     */
    private void initWidget() throws IOException {
        ShellSSHClient client = this.client();
        Charset charset = client.getCharset();
        ShellSSHTtyConnector connector = this.widget.createTtyConnector(charset);
        connector.init(client);
        ZModemPtyConnectorAdaptor adaptor = new ZModemPtyConnectorAdaptor(widget.getTerminal(), widget.getTerminalPanel(), connector);
        this.widget.openSession(adaptor);
        this.widget.onTermination(exitCode -> this.widget.close());
        connector.terminalSizeProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                this.termSize.text(newValue.getRows() + "x" + newValue.getColumns());
            }
        });
    }

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
        ShellSSHClient client = this.client();
        ShellSSHShell shell = client.openShell();
        this.initWidget();
        shell.connect(client.connectTimeout());
        if (!shell.isConnected()) {
            MessageBox.warn(I18nHelper.connectFail());
            this.closeTab();
            return;
        }
//        ShellConnect shellConnect = client.getShellConnect();
//        // macos需要初始化部分参数
//        if (client.isMacos() && shellConnect.getCharset() != null ) {
//            ShellSSHTtyConnector connector = this.widget.getTtyConnector();
//            connector.writeLine("export LANG=en_US." + shellConnect.getCharset());
//        }
        // 异步加载背景
        ThreadUtil.startVirtual(this::initBackground);
    }

    @Override
    public void onTabInit(RichTab tab) {
        super.onTabInit(tab);
        // 服务监控
        this.serverMonitor.selectedChanged((observable, oldValue, newValue) -> {
            if (newValue) {
                this.initMonitorTask();
            } else {
                this.closeMonitorTask();
            }
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
            this.serverMonitorTask = TaskManager.startInterval("ssh:eff_monitor:task", () -> {
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
            JulLog.debug("MonitorTask started.");
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
            JulLog.debug("MonitorTask closed.");
        } catch (Exception ex) {
            ex.printStackTrace();
            JulLog.error("close MonitorTask error", ex);
        }
    }

    /**
     * 终端历史
     */
    @FXML
    private void termHistory() {
        ShellViewFactory.termHistory(this.termHistory, this.client(), h -> {
            try {
                this.widget.getTtyConnector().writeLine(h);
            } catch (Exception ex) {
                MessageBox.exception(ex);
            }
        });
    }

    /**
     * 运行片段
     *
     * @param content 内容
     */
    public void runSnippet(String content) throws IOException {
        this.widget.getTtyConnector().write(content);
    }
}
