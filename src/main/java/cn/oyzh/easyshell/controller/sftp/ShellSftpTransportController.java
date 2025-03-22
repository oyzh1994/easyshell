package cn.oyzh.easyshell.controller.sftp;

import cn.oyzh.common.thread.DownLatch;
import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.fx.sftp.SftpTransportFileTableView;
import cn.oyzh.easyshell.fx.sftp.SftpTransportTaskTableView;
import cn.oyzh.easyshell.fx.ShellConnectComboBox;
import cn.oyzh.easyshell.sftp.SftpFile;
import cn.oyzh.easyshell.sftp.SftpUtil;
import cn.oyzh.easyshell.sftp.delete.SftpDeleteManager;
import cn.oyzh.easyshell.sftp.transport.SftpTransportManager;
import cn.oyzh.easyshell.sftp.transport.SftpTransportTask;
import cn.oyzh.easyshell.shell.ShellClient;
import cn.oyzh.easyshell.shell.ShellClientUtil;
import cn.oyzh.easyshell.util.ShellI18nHelper;
import cn.oyzh.fx.gui.combobox.CharsetComboBox;
import cn.oyzh.fx.gui.text.field.ClearableTextField;
import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.fx.plus.controller.StageController;
import cn.oyzh.fx.plus.controls.box.FXHBox;
import cn.oyzh.fx.plus.controls.box.FXVBox;
import cn.oyzh.fx.plus.controls.label.FXLabel;
import cn.oyzh.fx.plus.controls.toggle.FXToggleSwitch;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.node.NodeHeightResizer;
import cn.oyzh.fx.plus.window.StageAttribute;
import cn.oyzh.fx.plus.window.StageManager;
import cn.oyzh.i18n.I18nHelper;
import javafx.fxml.FXML;
import javafx.scene.Cursor;
import javafx.stage.WindowEvent;

import java.util.ArrayList;
import java.util.List;


/**
 * shell文件传输业务
 *
 * @author oyzh
 * @since 2025/03/21
 */
@StageAttribute(
        value = FXConst.FXML_PATH + "sftp/shellSftpTransport.fxml"
)
public class ShellSftpTransportController extends StageController {

    /**
     * 第一步
     */
    @FXML
    private FXVBox step1;

    /**
     * 第二步
     */
    @FXML
    private FXVBox step2;

    /**
     * 来源信息名称
     */
    @FXML
    private FXLabel sourceInfoName;

    /**
     * 目标信息名称
     */
    @FXML
    private FXLabel targetInfoName;

    /**
     * 来源信息
     */
    @FXML
    private ShellConnectComboBox sourceInfo;

    /**
     * 来源字符集
     */
    @FXML
    private CharsetComboBox sourceCharset;

    /**
     * 来源字符集名称
     */
    @FXML
    private FXLabel sourceCharsetName;

    /**
     * 目标信息
     */
    @FXML
    private ShellConnectComboBox targetInfo;

    /**
     * 目标字符集
     */
    @FXML
    private CharsetComboBox targetCharset;

    /**
     * 目标字符集名称
     */
    @FXML
    private FXLabel targetCharsetName;

    /**
     * 来源主机
     */
    @FXML
    private FXLabel sourceHost;

    /**
     * 目标主机
     */
    @FXML
    private FXLabel targetHost;

    /**
     * 来源文件
     */
    @FXML
    private SftpTransportFileTableView sourceFile;

    /**
     * 目标文件
     */
    @FXML
    private SftpTransportFileTableView targetFile;

    /**
     * 文件传输表
     */
    @FXML
    private SftpTransportTaskTableView transportTable;

    /**
     * 过滤来源文件
     */
    @FXML
    private ClearableTextField filterSourceFile;

    /**
     * 过滤目标文件
     */
    @FXML
    private ClearableTextField filterTargetFile;

    /**
     * 隐藏来源文件
     */
    @FXML
    private FXToggleSwitch hiddenSourceFile;

    /**
     * 隐藏目标文件
     */
    @FXML
    private FXToggleSwitch hiddenTargetFile;

    /**
     * 文件组件盒子
     */
    @FXML
    private FXHBox fileBox;

    /**
     * 来源客户端
     */
    private ShellClient sourceClient;

    /**
     * 目标客户端
     */
    private ShellClient targetClient;

    /**
     * 执行传输1
     */
    @FXML
    private void doTransport1() {
        try {
            List<SftpFile> files = this.sourceFile.getSelectedItems();
            if (CollectionUtil.isEmpty(files)) {
                return;
            }
            // 检查文件是否存在
            for (SftpFile file : files) {
                if (this.targetFile.existFile(file.getFileName()) && !MessageBox.confirm("[" + file.getFileName() + "] " + ShellI18nHelper.fileTip4())) {
                    return;
                }
            }
            String remotePath = this.targetFile.getCurrPath();
            this.doTransport(files, remotePath, this.sourceClient, this.targetClient);
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }

    /**
     * 执行传输2
     */
    @FXML
    private void doTransport2() {
        try {
            List<SftpFile> files = this.targetFile.getSelectedItems();
            if (CollectionUtil.isEmpty(files)) {
                return;
            }
            String remotePath = this.sourceFile.getCurrPath();
            this.doTransport(files, remotePath, this.targetClient, this.sourceClient);
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }

    /**
     * 执行传输
     *
     * @param files        文件
     * @param remotePath   远程路径
     * @param sourceClient 源连接
     * @param targetClient 目标连接
     */
    private void doTransport(List<SftpFile> files, String remotePath, ShellClient sourceClient, ShellClient targetClient) {
        for (SftpFile file : files) {
            if (file.isDirectory()) {
                sourceClient.transport(file, remotePath, targetClient);
            } else {
                String remoteFile = SftpUtil.concat(remotePath, file.getName());
                sourceClient.transport(file, remoteFile, targetClient);
            }
        }
    }

    /**
     * 初始化传输表
     */
    private void initTransportTable() {
        SftpTransportManager manager1 = this.sourceClient.getTransportManager();
        SftpTransportManager manager2 = this.targetClient.getTransportManager();
        List<SftpTransportTask> tasks = new ArrayList<>(manager1.getTasks());
        tasks.addAll(manager2.getTasks());
        this.transportTable.setItem(tasks.reversed());
    }

    @Override
    protected void bindListeners() {
        super.bindListeners();
        this.sourceInfo.selectedItemChanged((observable, oldValue, newValue) -> {
            if (newValue != null) {
                this.sourceHost.setText(newValue.getHost());
                this.sourceInfoName.setText(newValue.getName());
            } else {
                this.sourceHost.clear();
                this.sourceInfoName.clear();
            }
            if (this.sourceClient != null) {
                this.sourceClient.close();
                this.sourceClient = null;
            }
        });
        this.targetInfo.selectedItemChanged((observable, oldValue, newValue) -> {
            if (newValue != null) {
                this.targetHost.setText(newValue.getHost());
                this.targetInfoName.setText(newValue.getName());
            } else {
                this.targetHost.clear();
                this.targetInfoName.clear();
            }
            if (this.targetClient != null) {
                this.targetClient.close();
                this.targetClient = null;
            }
        });
        this.sourceCharset.selectedItemChanged((observable, oldValue, newValue) -> {
            if (newValue != null) {
                this.sourceCharsetName.setText(newValue);
            } else {
                this.sourceCharsetName.clear();
            }
        });
        this.targetCharset.selectedItemChanged((observable, oldValue, newValue) -> {
            if (newValue != null) {
                this.targetCharsetName.setText(newValue);
            } else {
                this.targetCharsetName.clear();
            }
        });
        // 隐藏文件处理
        this.hiddenSourceFile.selectedChanged((observable, oldValue, newValue) -> {
            if (newValue != null) {
                this.sourceFile.setShowHiddenFile(newValue);
            }
        });
        this.hiddenTargetFile.selectedChanged((observable, oldValue, newValue) -> {
            if (newValue != null) {
                this.targetFile.setShowHiddenFile(newValue);
            }
        });
        // 过滤内容处理
        this.filterSourceFile.addTextChangeListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                this.sourceFile.setFilterText(newValue);
            }
        });
        this.filterTargetFile.addTextChangeListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                this.targetFile.setFilterText(newValue);
            }
        });

        // 高度拉伸处理器
        NodeHeightResizer resizer = new NodeHeightResizer(this.transportTable, Cursor.DEFAULT, this::resizeBottom);
        resizer.widthLimit(200f, 450f);
        resizer.initResizeEvent();
    }

    /**
     * 底部组件重新布局
     *
     * @param newHeight 新宽度
     */
    private void resizeBottom(Float newHeight) {
        if (newHeight != null && !Float.isNaN(newHeight)) {
            // 设置组件宽
            this.transportTable.setRealHeight(newHeight);
            this.fileBox.setFlexHeight("100% - " + newHeight);
            this.transportTable.parentAutosize();
        }
    }

    @Override
    public void onWindowShown(WindowEvent event) {
        super.onWindowShown(event);
        // 来源连接不为null，则禁用来源选项
        ShellConnect sourceInfo = this.stage.getProp("sourceConnect");
        if (sourceInfo != null) {
            this.sourceInfo.select(sourceInfo);
            this.sourceInfo.disable();
        }
        this.stage.hideOnEscape();
    }

    @Override
    public String getViewTitle() {
        return I18nHelper.transportFile();
    }

    @FXML
    private void showStep2() {
        try {
            ShellConnect sourceInfo = this.sourceInfo.getSelectedItem();
            ShellConnect targetInfo = this.targetInfo.getSelectedItem();
            // 检查来源连接
            if (sourceInfo == null) {
                this.sourceInfo.requestFocus();
                MessageBox.warn(I18nHelper.pleaseSelectSourceConnect());
                return;
            }
            // 检查目标连接
            if (targetInfo == null) {
                this.targetInfo.requestFocus();
                MessageBox.warn(I18nHelper.pleaseSelectTargetConnect());
                return;
            }

            // 检查连接是否一样
            if (sourceInfo.compare(targetInfo)) {
                this.sourceInfo.requestFocus();
                MessageBox.warn(I18nHelper.connectionsCannotBeTheSame());
                return;
            }

            // 来源连接初始化
            if (this.sourceClient == null || this.sourceClient.isClosed()) {
                DownLatch latch = DownLatch.of();
                StageManager.showMask(() -> {
                    try {
                        this.sourceClient = ShellClientUtil.newClient(sourceInfo);
                        this.sourceClient.start(2500);
                    } finally {
                        latch.countDown();
                    }
                });
                if (!latch.await(3000) || !this.sourceClient.isConnected()) {
                    this.sourceClient.close();
                    this.sourceClient = null;
                    this.sourceInfo.requestFocus();
                    MessageBox.warn(sourceInfo.getName() + " " + I18nHelper.connectFail());
                    return;
                }
            }

            // 目标连接初始化
            if (this.targetClient == null || this.targetClient.isClosed()) {
                DownLatch latch = DownLatch.of();
                StageManager.showMask(() -> {
                    try {
                        this.targetClient = ShellClientUtil.newClient(targetInfo);
                        this.targetClient.start();
                    } finally {
                        latch.countDown();
                    }
                });
                if (!latch.await(5000) || !this.targetClient.isConnected()) {
                    this.targetClient.close();
                    this.targetClient = null;
                    this.targetInfo.requestFocus();
                    MessageBox.warn(targetInfo.getName() + " " + I18nHelper.connectFail());
                    return;
                }
            }
            // 显示页面
            this.step1.disappear();
            this.step2.display();
            // 初始化表格
            this.initFileTable();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }

    /**
     * 初始化文件表格
     */
    protected void initFileTable() {
        // 设置客户端
        this.sourceFile.setClient(this.sourceClient);
        this.targetFile.setClient(this.targetClient);
        // 传输处理器
        SftpTransportManager transportManager1 = this.sourceClient.getTransportManager();
        SftpTransportManager transportManager2 = this.targetClient.getTransportManager();
        // 注册监听器
        transportManager1.setTaskChangedCallback(this::initTransportTable);
        transportManager1.setMonitorEndedCallback(e -> {
            if (transportManager1.isCompleted()) {
                this.refreshTargetFile();
            }
        });
        transportManager2.setTaskChangedCallback(this::initTransportTable);
        transportManager2.setMonitorEndedCallback(e -> {
            if (transportManager2.isCompleted()) {
                this.refreshSourceFile();
            }
        });
        // 删除处理器
        SftpDeleteManager deleteManager1 = this.sourceClient.getSftpDeleteManager();
        SftpDeleteManager deleteManager2 = this.targetClient.getSftpDeleteManager();
        // 注册监听器
        deleteManager1.setDeleteDeletedCallback(f -> this.sourceFile.fileDeleted(f.getRemoteFile()));
        deleteManager2.setDeleteDeletedCallback(f -> this.targetFile.fileDeleted(f.getRemoteFile()));
        // 初始化文件树
        StageManager.showMask(() -> {
            try {
                this.sourceFile.loadFile();
                this.targetFile.loadFile();
            } catch (Exception ex) {
                ex.printStackTrace();
                MessageBox.exception(ex);
            }
        });
    }

    @FXML
    private void refreshSourceFile() {
        StageManager.showMask(() -> this.sourceFile.loadFile());
    }

    @FXML
    private void refreshTargetFile() {
        StageManager.showMask(() -> this.targetFile.loadFile());
    }
}
