package cn.oyzh.easyshell.tabs.ssh;

import cn.oyzh.common.log.JulLog;
import cn.oyzh.common.thread.ExecutorUtil;
import cn.oyzh.common.thread.TaskManager;
import cn.oyzh.common.thread.ThreadUtil;
import cn.oyzh.common.util.NumberUtil;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.event.file.ShellFileDraggedEvent;
import cn.oyzh.easyshell.file.ShellFileUtil;
import cn.oyzh.easyshell.fx.file.ShellFileLocationTextField;
import cn.oyzh.easyshell.fx.sftp.ShellSSHSFTPFileTableView;
import cn.oyzh.easyshell.sftp2.ShellSFTPClient;
import cn.oyzh.easyshell.sftp2.ShellSFTPFile;
import cn.oyzh.easyshell.ssh2.ShellSSHClient;
import cn.oyzh.easyshell.ssh2.ShellSSHTermWidget;
import cn.oyzh.easyshell.ssh2.ShellSSHTtyConnector;
import cn.oyzh.easyshell.ssh2.server.ShellServerExec;
import cn.oyzh.easyshell.ssh2.server.ShellServerMonitor;
import cn.oyzh.easyshell.tabs.ShellSnippetAdapter;
import cn.oyzh.easyshell.util.ShellConnectUtil;
import cn.oyzh.easyshell.util.ShellViewFactory;
import cn.oyzh.event.EventSubscribe;
import cn.oyzh.fx.gui.svg.pane.HiddenSVGPane;
import cn.oyzh.fx.gui.tabs.SubTabController;
import cn.oyzh.fx.gui.text.field.ClearableTextField;
import cn.oyzh.fx.plus.controls.box.FXVBox;
import cn.oyzh.fx.plus.controls.label.FXLabel;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.fx.plus.controls.svg.SVGLabel;
import cn.oyzh.fx.plus.controls.tab.FXTab;
import cn.oyzh.fx.plus.controls.text.FXText;
import cn.oyzh.fx.plus.controls.toggle.FXToggleSwitch;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.keyboard.KeyboardUtil;
import cn.oyzh.fx.plus.node.NodeWidthResizer;
import cn.oyzh.i18n.I18nHelper;
import com.jediterm.terminal.TtyConnector;
import com.jediterm.terminal.ui.FXTerminalPanel;
import javafx.collections.ListChangeListener;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.concurrent.Future;

/**
 * shell-效率模式tab内容组件
 *
 * @author oyzh
 * @since 2025/05/23
 */
public class ShellSSHEffTabController extends SubTabController implements ShellSnippetAdapter {

    /**
     * 左侧组件
     */
    @FXML
    private FXVBox leftBox;

    /**
     * 右侧组件
     */
    @FXML
    private FXVBox rightBox;

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
     * 显示文件
     */
    @FXML
    private FXToggleSwitch showFile;

    /**
     * 终端大小
     */
    @FXML
    private FXText termSize;

    /**
     * 文件信息
     */
    @FXML
    private FXLabel fileInfo;

    // /**
    //  * 设置
    //  */
    // private final ShellSetting setting = ShellSettingStore.SETTING;
    //
    // /**
    //  * 设置储存
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
        //     this.rightBox.removeChild(this.widget);
        //     TtyConnector connector = this.widget.getTtyConnector();
        //     if (connector != null) {
        //         connector.close();
        //     }
        // }
        // // 已关闭
        // ShellSSHClient client = this.client();
        // ChannelShell shell = client.openShell();
        // if (shell == null) {
        //     throw new Exception("shell is null");
        // }
        // if (!shell.isOpen()) {
        //     shell.open().verify(client.connectTimeout());
        // }
        // // 已关闭
        // if (shell == null) {
        //     this.closeTab();
        //     return;
        // }
        // 初始化组件
        // this.widget = new ShellSSHTermWidget();
        // this.widget.setFlexWidth("100%");
        // this.widget.setFlexHeight("100% - 30");
        // this.rightBox.addChild(0, this.widget);
        // 初始化退格码
        this.widget.initBackspaceCode(this.shellConnect().getBackspaceType());
        // 设置alt修饰
        this.widget.setAltSendsEscape(this.shellConnect().isAltSendsEscape());
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
    //     // ShellSSHClient client = this.client();
    //     AtomicReference<Exception> ref = new AtomicReference<>();
    //     ThreadUtil.start(() -> {
    //         try {
    //             // ChannelShell shell = client.reopenShell();
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
     * 初始化文件
     */
    private void initFile() {
        this.fileTable.setSSHClient(this.client());
        this.fileTable.setEnabledLoading(false);
        this.fileTable.refreshFile();
        this.fileTable.setEnabledLoading(true);
        ShellSFTPClient sftpClient = this.sftpClient();
        // 任务数量监听
        sftpClient.addTaskSizeListener(() -> {
            if (sftpClient.isTaskEmpty("upload,download")) {
                this.manage.clear();
            } else {
                this.manage.text("(" + sftpClient.getTaskSize() + ")");
            }
        }, "upload,download");
        // 监听终端目录
        this.client().workDirProperty().addListener((observable, oldValue, newValue) -> {
            if (this.client().isResolveWorkerDir()) {
                this.fileTable.setEnabledLoading(false);
                this.fileTable.cd(newValue);
                this.fileTable.setEnabledLoading(true);
            }
        });
        // 设置收藏处理
        this.location.setFileCollectSupplier(() -> ShellFileUtil.fileCollect(this.sftpClient()));
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
        // shell.connect(client.connectTimeout());
        // if (!shell.isOpen()) {
        //     MessageBox.warn(I18nHelper.connectFail());
        //     this.closeTab();
        //     return;
        // }
        // if (!this.openShell()) {
        //     return;
        // }
        // 初始化文件
        this.initFile();
        // 初始化
        ShellConnect connect = this.shellConnect();
        // 初始化
        this.showFile.setSelected(connect.isShowFile());
        this.serverMonitor.setSelected(connect.isServerMonitor());
        this.followTerminalDir.setSelected(connect.isFollowTerminalDir());
        // 显示隐藏文件
        this.hiddenFile(this.shellConnect().isShowHiddenFile());
        // 异步加载背景
        ThreadUtil.startVirtual(this::initBackground);
    }

    @Override
    public void onTabInit(FXTab tab) {
        super.onTabInit(tab);
        //// 快捷键
        //this.root.getContent().addEventFilter(KeyEvent.KEY_PRESSED, event -> {
        //    if (KeyboardUtil.hide_keyCombination.match(event)) {
        //        this.hiddenFile();
        //    }
        //});
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
            // 解析路径
            this.client().setResolveWorkerDir(newValue);
            // 设置
            this.shellConnect().setFollowTerminalDir(newValue);
            // this.setting.setSshFollowTerminalDir(newValue);
            // this.settingStore.update(this.setting);
        });
        // 服务监控
        this.serverMonitor.selectedChanged((observable, oldValue, newValue) -> {
            if (newValue) {
                this.initMonitorTask();
            } else {
                this.closeMonitorTask();
            }
            // 设置
            this.shellConnect().setServerMonitor(newValue);
            // this.setting.setSshServerMonitor(newValue);
            // this.settingStore.update(this.setting);
        });
        // 文件列表
        this.showFile.selectedChanged((observable, oldValue, newValue) -> {
            if (newValue) {
                this.client().setResolveWorkerDir(this.followTerminalDir.isSelected());
                this.initFileBox();
            } else {
                this.client().setResolveWorkerDir(false);
                this.hideFileBox();
            }
            // 存储
            this.shellConnect().setShowFile(newValue);
            // this.setting.setSshShowFile(newValue);
            // this.settingStore.update(this.setting);
        });
        // 文件过滤
        this.filterFile.addTextChangeListener((observableValue, aBoolean, t1) -> {
            try {
                this.fileTable.setFilterText(t1);
            } catch (Exception ex) {
                MessageBox.exception(ex);
            }
        });
        // 监听信息
        this.fileTable.itemList().addListener((ListChangeListener<ShellSFTPFile>) c -> {
            if (this.showFile.isSelected()) {
                this.fileInfo.setText(this.fileTable.fileInfo());
            }
        });
        // 绑定提示快捷键
        //this.hiddenPane.setTipKeyCombination(KeyboardUtil.hide_keyCombination);
        this.refreshFile.setTipKeyCombination(KeyboardUtil.refresh_keyCombination);
        this.filterFile.setTipKeyCombination(KeyboardUtil.search_keyCombination);
        this.deleteFile.setTipKeyCombination(KeyboardUtil.delete_keyCombination);

        // 创建拉伸处理器
        NodeWidthResizer.of(this.leftBox, this::onLeftResized, 260f, 750f);
    }

    /**
     * 左侧拉伸事件
     *
     * @param newWidth 新宽度
     */
    private void onLeftResized(float newWidth) {
        this.leftBox.setRealWidth(newWidth);
        this.rightBox.setLayoutX(newWidth);
        this.rightBox.setFlexWidth("100% - " + newWidth);
        this.rightBox.parentAutosize();
    }

    @Override
    public void onTabClosed(Event event) {
        super.onTabClosed(event);
        if (this.widget != null) {
            this.widget.close();
        }
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
            if (this.fileTable.isPkgTransfer()) {
                this.fileTable.updateByPkg(files, this.client());
            } else {
                this.fileTable.uploadFile(files);
            }
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
        this.hiddenFile(this.hiddenPane.isHidden());
    }

    /**
     * 隐藏文件
     *
     * @param showHidden 是否显示隐藏文件
     */
    private void hiddenFile(boolean showHidden) {
        if (!showHidden) {
            this.hiddenPane.hidden();
            this.fileTable.setShowHiddenFile(false);
            this.hiddenPane.setTipText(I18nHelper.showHiddenFiles());
        } else {
            this.hiddenPane.show();
            this.fileTable.setShowHiddenFile(true);
            this.hiddenPane.setTipText(I18nHelper.doNotShowHiddenFiles());
        }
        // 设置
        this.shellConnect().setShowHiddenFile(showHidden);
        // this.setting.setShowHiddenFile(hidden);
        // this.settingStore.update(this.setting);
    }

    /**
     * 初始化文件组件
     */
    private void initFileBox() {
        this.leftBox.display();
        this.rightBox.setLayoutX(this.leftBox.getRealWidth());
        this.rightBox.setFlexWidth("100% - " + this.leftBox.getRealWidth());
        this.rightBox.parentAutosize();
    }

    /**
     * 隐藏文件组件
     */
    private void hideFileBox() {
        this.leftBox.disappear();
        this.rightBox.setLayoutX(0);
        this.rightBox.setFlexWidth("100%");
        this.rightBox.parentAutosize();
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
            JulLog.error("close monitorTask error", ex);
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

    /**
     * 复制路径到终端
     */
    @FXML
    private void copyPathToTerminal() {
        try {
            this.widget.getTtyConnector().write(this.fileTable.getLocation());
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }
}
