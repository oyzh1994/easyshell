package cn.oyzh.easyshell.tabs.ssh;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.thread.ExecutorUtil;
import cn.oyzh.common.thread.TaskManager;
import cn.oyzh.common.thread.ThreadUtil;
import cn.oyzh.common.util.NumberUtil;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.domain.ShellSetting;
import cn.oyzh.easyshell.event.file.ShellFileDraggedEvent;
import cn.oyzh.easyshell.fx.file.ShellFileLocationTextField;
import cn.oyzh.easyshell.fx.sftp.ShellSSHSFTPFileTableView;
import cn.oyzh.easyshell.internal.server.ShellServerExec;
import cn.oyzh.easyshell.internal.server.ShellServerMonitor;
import cn.oyzh.easyshell.sftp.ShellSFTPClient;
import cn.oyzh.easyshell.ssh.ShellSSHClient;
import cn.oyzh.easyshell.ssh.ShellSSHShell;
import cn.oyzh.easyshell.ssh.ShellSSHTermWidget;
import cn.oyzh.easyshell.ssh.ShellSSHTtyConnector;
import cn.oyzh.easyshell.store.ShellSettingStore;
import cn.oyzh.easyshell.util.ShellConnectUtil;
import cn.oyzh.easyshell.util.ShellViewFactory;
import cn.oyzh.event.EventSubscribe;
import cn.oyzh.fx.gui.svg.pane.HiddenSVGPane;
import cn.oyzh.fx.gui.tabs.RichTab;
import cn.oyzh.fx.gui.tabs.SubTabController;
import cn.oyzh.fx.gui.text.field.ClearableTextField;
import cn.oyzh.fx.plus.controls.label.FXLabel;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.fx.plus.controls.svg.SVGLabel;
import cn.oyzh.fx.plus.controls.tab.FXTab;
import cn.oyzh.fx.plus.controls.toggle.FXToggleSwitch;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.keyboard.KeyboardUtil;
import cn.oyzh.i18n.I18nHelper;
import cn.oyzh.jeditermfx.terminal.ui.FXHyperlinkFilter;
import com.jediterm.core.util.TermSize;
import com.jediterm.terminal.TtyConnector;
import com.jediterm.terminal.ui.FXFXTerminalPanel;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.input.KeyEvent;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.concurrent.Future;

/**
 * ssh-终端tab内容组件
 *
 * @author oyzh
 * @since 2025/03/11
 */
public class ShellSSHEffTabController extends SubTabController {

    /**
     * 终端组件
     */
    @FXML
    private ShellSSHTermWidget widget;

    /**
     * tab
     */
    @FXML
    private FXTab root;

    /**
     * 当前位置
     */
    @FXML
    private ShellFileLocationTextField location;

    /**
     * 上传/下载管理
     */
    @FXML
    private SVGLabel manage;

    /**
     * 刷新文件
     */
    @FXML
    private SVGGlyph refreshFile;

    /**
     * 删除文件
     */
    @FXML
    private SVGGlyph deleteFile;

    /**
     * 隐藏文件
     */
    @FXML
    private HiddenSVGPane hiddenPane;

    /**
     * 文件表格
     */
    @FXML
    private ShellSSHSFTPFileTableView fileTable;

    /**
     * 文件过滤
     */
    @FXML
    private ClearableTextField filterFile;

    /**
     * 跟随终端目录
     */
    @FXML
    private FXToggleSwitch followTerminalDir;

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
     * 设置
     */
    private final ShellSetting setting = ShellSettingStore.SETTING;

    /**
     * 设置储存
     */
    private final ShellSettingStore settingStore = ShellSettingStore.INSTANCE;

    /**
     * 初始化终端
     *
     * @throws IOException 异常
     */
    private void initWidget() throws IOException {
        Charset charset = this.client().getCharset();
        ShellSSHTtyConnector connector = this.widget.createTtyConnector(charset);
        connector.initShell(this.client());
        this.widget.openSession(connector);
        this.widget.onTermination(exitCode -> this.widget.close());
        this.widget.addHyperlinkFilter(new FXHyperlinkFilter());
        connector.terminalSizeProperty().addListener((observable, oldValue, newValue) -> this.initShellSize());
    }

    /**
     * 初始化终端大小
     */
    private void initShellSize() {
        int sizeW = (int) this.widget.getTerminalPanel().getWidth();
        int sizeH = (int) this.widget.getTerminalPanel().getHeight();
        TermSize termSize = this.widget.getTermSize();
        ShellSSHShell shell = this.client().getShell();
        shell.setPtySize(termSize.getColumns(), termSize.getRows(), sizeW, sizeH);
    }

    /**
     * 初始化背景
     */
    private void initBackground() {
        ShellConnect connect = this.client().getShellConnect();
        FXFXTerminalPanel terminalPanel = this.widget.getTerminalPanel();
        // 处理背景
        ShellConnectUtil.initBackground(connect, terminalPanel);
    }

    /**
     * 初始化文件
     */
    private void initFile() {
        this.fileTable.setSSHClient(this.client());
        this.fileTable.refreshFile();
        ShellSFTPClient sftpClient = this.sftpClient();
        // 任务数量监听
        sftpClient.addTaskSizeListener(() -> {
            if (sftpClient.isTaskEmpty()) {
                this.manage.clear();
            } else {
                this.manage.text("(" + sftpClient.getTaskSize() + ")");
            }
        });
        // 监听终端目录
        this.client().workDirProperty().addListener((observable, oldValue, newValue) -> {
            if (this.client().isResolveWorkerDir()) {
                this.fileTable.setEnabledLoading(false);
                this.fileTable.cd(newValue);
                this.fileTable.setEnabledLoading(true);
            }
        });
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
        ShellConnect shellConnect = client.getShellConnect();
        // macos需要初始化部分参数
        if (client.isMacos() && shellConnect.getCharset() != null) {
            TtyConnector connector = this.widget.getTtyConnector();
            connector.write("export LANG=en_US." + shellConnect.getCharset() + "\n");
        }
        // 初始化文件
        this.initFile();
        // 异步加载背景
        ThreadUtil.startVirtual(this::initBackground);
    }

    @Override
    public void onTabInit(RichTab tab) {
        super.onTabInit(tab);
        // 快捷键
        this.root.getContent().addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (KeyboardUtil.hide_keyCombination.match(event)) {
                this.hiddenFile();
            }
        });
        // 路径跳转
        this.location.setOnJumpLocation(path -> {
            this.fileTable.cd(path);
        });
        // 监听位置
        this.fileTable.locationProperty().addListener((observableValue, aBoolean, t1) -> {
            if (t1 == null) {
                this.location.clear();
            } else {
                this.location.text(t1);
            }
        });
        // 跟随终端目录
        this.followTerminalDir.selectedChanged((observable, oldValue, newValue) -> {
            this.client().setResolveWorkerDir(newValue);
        });
        // 服务监控
        this.serverMonitor.selectedChanged((observable, oldValue, newValue) -> {
            if (newValue) {
                this.initMonitorTask();
            } else {
                this.closeMonitorTask();
            }
        });
        // 绑定提示快捷键
        this.hiddenPane.setTipKeyCombination(KeyboardUtil.hide_keyCombination);
        this.refreshFile.setTipKeyCombination(KeyboardUtil.refresh_keyCombination);
        this.filterFile.setTipKeyCombination(KeyboardUtil.search_keyCombination);
        this.deleteFile.setTipKeyCombination(KeyboardUtil.delete_keyCombination);
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

    public ShellSFTPClient sftpClient() {
        return this.client().sftpClient();
    }

    /**
     * 刷新文件
     */
    @FXML
    private void refreshFile() {
        try {
            this.fileTable.loadFile();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }

    /**
     * 删除文件
     */
    @FXML
    private void deleteFile() {
        try {
            this.fileTable.deleteFile(this.fileTable.getSelectedItems());
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }

    /**
     * 返回上一级
     */
    @FXML
    private void returnDir() {
        try {
            this.fileTable.returnDir();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }

    /**
     * 进入home目录
     */
    @FXML
    private void intoHome() {
        try {
            this.fileTable.intoHome();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }

    /**
     * 创建文件夹
     */
    @FXML
    private void mkdir() {
        this.fileTable.createDir();
    }

    /**
     * 创建文件
     */
    @FXML
    private void touchFile() {
        this.fileTable.touch();
    }

    /**
     * 上传文件
     */
    @FXML
    private void uploadFile() {
        this.fileTable.uploadFile();
    }

    /**
     * 上传文件夹
     */
    @FXML
    private void uploadFolder() {
        this.fileTable.uploadFolder();
    }

    /**
     * 文件拖拽事件
     *
     * @param event 事件
     */
    @EventSubscribe
    private void draggedFile(ShellFileDraggedEvent event) {
        try {
            // 判断是否选中
            if (!this.root.isSelected() || !this.getTab().isSelected()) {
                return;
            }
            List<File> files = event.data();
            this.fileTable.uploadFile(files);
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }

    /**
     * 管理上传、下载
     */
    @FXML
    private void manage() {
        ShellViewFactory.fileManage(this.client().sftpClient());
    }

    /**
     * 隐藏文件
     */
    @FXML
    private void hiddenFile() {
        this.hiddenFile(!this.hiddenPane.isHidden());
    }

    /**
     * 隐藏文件
     *
     * @param hidden 是否隐藏
     */
    private void hiddenFile(boolean hidden) {
        if (hidden) {
            this.hiddenPane.hidden();
            this.fileTable.setShowHiddenFile(false);
            this.hiddenPane.setTipText(I18nHelper.showHiddenFiles());
        } else {
            this.hiddenPane.show();
            this.fileTable.setShowHiddenFile(true);
            this.hiddenPane.setTipText(I18nHelper.doNotShowHiddenFiles());
        }
        this.setting.setShowHiddenFile(hidden);
        this.settingStore.update(this.setting);
    }

    /**
     * 初始化监控任务
     */
    private void initMonitorTask() {
        // 处理组件
        this.widget.setFlexHeight("100% - 30");
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
            // 处理组件
            this.widget.setFlexHeight("100%");
            this.serverMonitorInfo.clear();
            this.serverMonitorInfo.disappear();
            JulLog.debug("MonitorTask closed.");
        } catch (Exception ex) {
            ex.printStackTrace();
            JulLog.error("close monitorTask error", ex);
        }
    }

}
