package cn.oyzh.easyshell.controller.sftp;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.fx.ShellConnectComboBox;
import cn.oyzh.easyshell.fx.sftp.SftpLocationTextField;
import cn.oyzh.easyshell.fx.sftp.SftpTransportFileTableView;
import cn.oyzh.easyshell.fx.svg.glyph.file.FileSVGGlyph;
import cn.oyzh.easyshell.sftp.ShellSftpFile;
import cn.oyzh.easyshell.sftp.ShellSftpUtil;
import cn.oyzh.easyshell.sftp.delete.SftpDeleteManager;
import cn.oyzh.easyshell.sftp.transport.SftpTransportManager;
import cn.oyzh.easyshell.sftp.transport.SftpTransportMonitor;
import cn.oyzh.easyshell.sftp.transport.SftpTransportTask;
import cn.oyzh.easyshell.shell.ShellClient;
import cn.oyzh.easyshell.shell.ShellClientUtil;
import cn.oyzh.easyshell.util.ShellI18nHelper;
import cn.oyzh.fx.gui.svg.pane.HiddenSVGPane;
import cn.oyzh.fx.gui.text.field.ClearableTextField;
import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.fx.plus.controller.StageController;
import cn.oyzh.fx.plus.controls.FXProgressTextBar;
import cn.oyzh.fx.plus.controls.box.FXHBox;
import cn.oyzh.fx.plus.controls.box.FXVBox;
import cn.oyzh.fx.plus.controls.label.FXLabel;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.util.AnimationUtil;
import cn.oyzh.fx.plus.validator.ValidatorUtil;
import cn.oyzh.fx.plus.window.StageAttribute;
import cn.oyzh.fx.plus.window.StageManager;
import cn.oyzh.i18n.I18nHelper;
import javafx.fxml.FXML;
import javafx.stage.WindowEvent;

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

//    /**
//     * 来源字符集
//     */
//    @FXML
//    private CharsetComboBox sourceCharset;
//
//    /**
//     * 来源字符集名称
//     */
//    @FXML
//    private FXLabel sourceCharsetName;

    /**
     * 目标信息
     */
    @FXML
    private ShellConnectComboBox targetInfo;

//    /**
//     * 目标字符集
//     */
//    @FXML
//    private CharsetComboBox targetCharset;
//
//    /**
//     * 目标字符集名称
//     */
//    @FXML
//    private FXLabel targetCharsetName;

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

//    /**
//     * 文件传输表
//     */
//    @FXML
//    private SftpTransportTaskTableView transportTable;

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
     * 来源当前位置
     */
    @FXML
    private SftpLocationTextField sourceLocation;

    /**
     * 目标当前位置
     */
    @FXML
    private SftpLocationTextField targetLocation;

//    /**
//     * 隐藏来源文件
//     */
//    @FXML
//    private FXToggleSwitch hiddenSourceFile;
//
//    /**
//     * 隐藏目标文件
//     */
//    @FXML
//    private FXToggleSwitch hiddenTargetFile;

    /**
     * 隐藏来源文件
     */
    @FXML
    private HiddenSVGPane hiddenSourcePane;

    /**
     * 隐藏目标文件
     */
    @FXML
    private HiddenSVGPane hiddenTargetPane;

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
     * 源名称
     */
    @FXML
    private FXLabel sourceName;

    /**
     * 源传输组件
     */
    @FXML
    private FXHBox sourceTransportBox;

    /**
     * 源传输标签
     */
    @FXML
    private FXLabel sourceFileTransport;

    /**
     * 源传输进度
     */
    @FXML
    private FXProgressTextBar sourceTransportProgress;

    /**
     * 目标名称
     */
    @FXML
    private FXLabel targetName;

    /**
     * 目标传输组件
     */
    @FXML
    private FXHBox targetTransportBox;

    /**
     * 目标传输标签
     */
    @FXML
    private FXLabel targetFileTransport;

    /**
     * 目标传输进度
     */
    @FXML
    private FXProgressTextBar targetTransportProgress;

    /**
     * 执行传输1
     */
//    @FXML
    private void doTransport1(List<ShellSftpFile> files) {
        try {
//            List<ShellSftpFile> files = this.sourceFile.getSelectedItems();
            if (CollectionUtil.isEmpty(files)) {
                return;
            }
            // 检查文件是否存在
            for (ShellSftpFile file : files) {
                if (file.isCurrentFile() || file.isReturnDirectory()) {
                    continue;
                }
                if (this.targetFile.existFile(file.getFileName()) && !MessageBox.confirm("[" + file.getFileName() + "] " + ShellI18nHelper.fileTip4())) {
                    return;
                }
            }
            String remotePath = this.targetFile.getLocation();
            this.doTransport(files, remotePath, this.sourceClient, this.targetClient);
            AnimationUtil.move(new FileSVGGlyph("150"), this.sourceFile, this.sourceTransportBox);
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }

    /**
     * 执行传输2
     */
//    @FXML
    private void doTransport2(List<ShellSftpFile> files) {
        try {
//            List<ShellSftpFile> files = this.targetFile.getSelectedItems();
            if (CollectionUtil.isEmpty(files)) {
                return;
            }
            // 检查文件是否存在
            for (ShellSftpFile file : files) {
                if (file.isCurrentFile() || file.isReturnDirectory()) {
                    continue;
                }
                if (this.sourceFile.existFile(file.getFileName()) && !MessageBox.confirm("[" + file.getFileName() + "] " + ShellI18nHelper.fileTip4())) {
                    return;
                }
            }
            String remotePath = this.sourceFile.getLocation();
            this.doTransport(files, remotePath, this.targetClient, this.sourceClient);
            AnimationUtil.move(new FileSVGGlyph("150"), this.targetFile, this.targetTransportBox);
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
    private void doTransport(List<ShellSftpFile> files, String remotePath, ShellClient sourceClient, ShellClient targetClient) {
        for (ShellSftpFile file : files) {
            if (file.isCurrentFile() || file.isReturnDirectory()) {
                continue;
            }
            if (file.isDirectory()) {
                sourceClient.transport(file, remotePath, targetClient);
            } else {
                String remoteFile = ShellSftpUtil.concat(remotePath, file.getName());
                sourceClient.transport(file, remoteFile, targetClient);
            }
        }
    }

//    /**
//     * 初始化传输表
//     */
//    private void initTransportTable() {
//        SftpTransportManager manager1 = this.sourceClient.getTransportManager();
//        SftpTransportManager manager2 = this.targetClient.getTransportManager();
//        List<SftpTransportTask> tasks = new ArrayList<>(manager1.getTasks());
//        tasks.addAll(manager2.getTasks());
//        this.transportTable.setItem(tasks.reversed());
//    }

    @Override
    protected void bindListeners() {
        super.bindListeners();
        this.sourceInfo.selectedItemChanged((observable, oldValue, newValue) -> {
            if (newValue != null) {
                // 初始化名称
                this.sourceName.setText(newValue.getName());
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
                // 初始化名称
                this.targetName.setText(newValue.getName());
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
//        this.sourceCharset.selectedItemChanged((observable, oldValue, newValue) -> {
//            if (newValue != null) {
//                this.sourceCharsetName.setText(newValue);
//            } else {
//                this.sourceCharsetName.clear();
//            }
//        });
//        this.targetCharset.selectedItemChanged((observable, oldValue, newValue) -> {
//            if (newValue != null) {
//                this.targetCharsetName.setText(newValue);
//            } else {
//                this.targetCharsetName.clear();
//            }
//        });
//        // 隐藏文件处理
//        this.hiddenSourceFile.selectedChanged((observable, oldValue, newValue) -> {
//            if (newValue != null) {
//                this.sourceFile.setShowHiddenFile(newValue);
//            }
//        });
//        this.hiddenTargetFile.selectedChanged((observable, oldValue, newValue) -> {
//            if (newValue != null) {
//                this.targetFile.setShowHiddenFile(newValue);
//            }
//        });
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

//        // 高度拉伸处理器
//        NodeHeightResizer resizer = new NodeHeightResizer(this.transportTable, Cursor.DEFAULT, this::resizeBottom);
//        resizer.widthLimit(200f, 450f);
//        resizer.initResizeEvent();
    }

//    /**
//     * 底部组件重新布局
//     *
//     * @param newHeight 新宽度
//     */
//    private void resizeBottom(Float newHeight) {
//        if (newHeight != null && !Float.isNaN(newHeight)) {
//            // 设置组件宽
//            this.transportTable.setRealHeight(newHeight);
//            this.fileBox.setFlexHeight("100% - " + newHeight);
//            this.transportTable.parentAutosize();
//        }
//    }

    @Override
    public void onWindowShown(WindowEvent event) {
        super.onWindowShown(event);
        // 来源连接不为null，则禁用来源选项
        ShellConnect sourceInfo = this.stage.getProp("sourceConnect");
        if (sourceInfo != null) {
            this.sourceInfo.select(sourceInfo);
            this.sourceInfo.disable();
        }
//        this.stage.hideOnEscape();
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
//                this.sourceInfo.requestFocus();
//                MessageBox.warn(I18nHelper.pleaseSelectSourceConnect());
                ValidatorUtil.validFail(this.sourceInfo);
                return;
            }
            // 检查目标连接
            if (targetInfo == null) {
//                this.targetInfo.requestFocus();
//                MessageBox.warn(I18nHelper.pleaseSelectTargetConnect());
                ValidatorUtil.validFail(this.targetInfo);
                return;
            }

            // 检查连接是否一样
            if (sourceInfo.compare(targetInfo)) {
//                this.sourceInfo.requestFocus();
//                MessageBox.warn(I18nHelper.connectionsCannotBeTheSame());
                ValidatorUtil.validFail(this.sourceInfo);
                return;
            }

            // 连接初始化
            if (this.sourceClient == null || this.sourceClient.isClosed() || this.targetClient == null || this.targetClient.isClosed()) {
                StageManager.showMask(() -> {
                    try {
                        // 检查来源
                        if (this.sourceClient == null || this.sourceClient.isClosed()) {
                            this.sourceClient = ShellClientUtil.newClient(sourceInfo);
                            this.sourceClient.start(2500);
                        }
                        if (!this.sourceClient.isConnected()) {
                            this.sourceClient.close();
                            this.sourceClient = null;
                            this.sourceInfo.requestFocus();
                            MessageBox.warn(sourceInfo.getName() + " " + I18nHelper.connectFail());
                            return;
                        }
                        // 检查目标
                        if (this.targetClient == null || this.targetClient.isClosed()) {
                            this.targetClient = ShellClientUtil.newClient(targetInfo);
                            this.targetClient.start(2500);
                        }
                        if (!this.targetClient.isConnected()) {
                            this.targetClient.close();
                            this.targetClient = null;
                            this.targetInfo.requestFocus();
                            MessageBox.warn(targetInfo.getName() + " " + I18nHelper.connectFail());
                            return;
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
                });
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }

    private SftpTransportManager sourceTransportManager;

    private SftpTransportManager targetTransportManager;

    /**
     * 初始化文件表格
     */
    protected void initFileTable() {
        // 设置客户端
        this.sourceFile.setClient(this.sourceClient);
        this.targetFile.setClient(this.targetClient);
        // 传输处理器
        this.sourceTransportManager = this.sourceClient.getTransportManager();
        this.targetTransportManager = this.targetClient.getTransportManager();
        // 注册监听器
        this.sourceTransportManager.addMonitorChangedCallback(this, this::sourceTransportMonitorChanged);
        this.sourceTransportManager.addTaskSizeChangedCallback(this, this::sourceTransportTaskSizeChanged);
        this.sourceTransportManager.addTaskStatusChangedCallback(this, this::sourceTransportStatusChanged);
//        this.sourceTransportManager.setMonitorEndedCallback(e -> {
//            if (this.sourceTransportManager.isCompleted()) {
//                this.refreshTargetFile();
//            }
//        });
        this.targetTransportManager.addMonitorChangedCallback(this, this::targetTransportMonitorChanged);
        this.targetTransportManager.addTaskSizeChangedCallback(this, this::targetTransportTaskSizeChanged);
        this.targetTransportManager.addTaskStatusChangedCallback(this, this::targetTransportStatusChanged);
//        this.targetTransportManager.setMonitorEndedCallback(e -> {
//            if (this.targetTransportManager.isCompleted()) {
//                this.refreshSourceFile();
//            }
//        });
        // 删除处理器
        SftpDeleteManager deleteManager1 = this.sourceClient.getDeleteManager();
        SftpDeleteManager deleteManager2 = this.targetClient.getDeleteManager();
        // 注册监听器
        deleteManager1.addDeleteDeletedCallback(this, f -> this.sourceFile.fileDeleted(f));
        deleteManager2.addDeleteDeletedCallback(this, f -> this.targetFile.fileDeleted(f));
        // 监听位置
        this.sourceFile.locationProperty().addListener((observable, oldValue, t1) -> {
            if (t1 == null) {
                this.sourceLocation.clear();
            } else {
                this.sourceLocation.text(t1);
            }
        });
        this.targetFile.locationProperty().addListener((observable, oldValue, t1) -> {
            if (t1 == null) {
                this.targetLocation.clear();
            } else {
                this.targetLocation.text(t1);
            }
        });
        // 路径跳转
        this.sourceLocation.setOnJumpLocation(path -> {
            this.sourceFile.cd(path);
        });
        this.targetLocation.setOnJumpLocation(path -> {
            this.targetFile.cd(path);
        });
        // 绑定传输回调
        this.sourceFile.setTransportCallback(this::doTransport1);
        this.targetFile.setTransportCallback(this::doTransport2);
        // 初始化文件树
        this.sourceFile.loadFile();
        this.targetFile.loadFile();
    }

    @FXML
    private void refreshSourceFile() {
        StageManager.showMask(() -> this.sourceFile.loadFile());
    }

    @FXML
    private void refreshTargetFile() {
        StageManager.showMask(() -> this.targetFile.loadFile());
    }

    @Override
    public void onWindowCloseRequest(WindowEvent event) {
        // 检查任务是否执行中
        SftpTransportManager manager1 = this.sourceClient == null ? null : this.sourceClient.getTransportManager();
        if (manager1 != null
                && !manager1.isCompleted()
                && !MessageBox.confirm(ShellI18nHelper.fileTip18())) {
            event.consume();
            return;
        }
        // 检查任务是否执行中
        SftpTransportManager manager2 = this.targetClient == null ? null : this.targetClient.getTransportManager();
        if (manager2 != null
                && !manager2.isCompleted()
                && !MessageBox.confirm(ShellI18nHelper.fileTip18())) {
            event.consume();
            return;
        }
        // 取消任务
        if (manager1 != null) {
            manager1.cancel();
        }
        if (manager2 != null) {
            manager2.cancel();
        }
        // 关闭连接
        if (this.sourceClient != null) {
            this.sourceClient.close();
        }
        if (this.targetClient != null) {
            this.targetClient.close();
        }
        super.onWindowCloseRequest(event);
    }

    /**
     * 取消源传输
     */
    @FXML
    private void cancelSourceTransport() {
        this.sourceTransportManager.cancel();
    }

    /**
     * 取消目标传输
     */
    @FXML
    private void cancelTargetTransport() {
        this.targetTransportManager.cancel();
    }

    /**
     * 源状态改变事件
     *
     * @param status 状态
     * @param task   任务
     */
    private void sourceTransportStatusChanged(String status, SftpTransportTask task) {
        StringBuilder builder = new StringBuilder();
        builder.append(I18nHelper.task()).append(": ").append(this.sourceTransportManager.getTaskSize());
        builder.append(" ").append(I18nHelper.status()).append(": ").append(status);
        builder.append(" ").append(I18nHelper.src()).append(": ").append(task.getSrcPath());
        builder.append(" ").append(I18nHelper.dest()).append(": ").append(task.getDestPath());
        this.sourceFileTransport.text(builder.toString());
        this.sourceTransportProgress.setValue(task.getCurrentSize(), task.getTotalSize());
    }

    /**
     * 源监听变更事件
     *
     * @param monitor 监听器
     * @param task    任务
     */
    private void sourceTransportMonitorChanged(SftpTransportMonitor monitor, SftpTransportTask task) {
        StringBuilder builder = new StringBuilder();
        builder.append(I18nHelper.task()).append(": ").append(this.sourceTransportManager.getTaskSize());
//        builder.append(" ").append(I18nHelper.count()).append(": ").append(task.size());
        builder.append(" ").append(I18nHelper.speed()).append(": ").append(task.getSpeed());
        builder.append(" ").append(I18nHelper.size()).append(": ").append(task.getFileSize());
        builder.append(" ").append(I18nHelper.src()).append(": ").append(task.getSrcPath());
        builder.append(" ").append(I18nHelper.dest()).append(": ").append(task.getDestPath());
        builder.append(" ").append(I18nHelper.current()).append(": ").append(monitor.getLocalFileName());
        this.sourceFileTransport.text(builder.toString());
        this.sourceTransportProgress.setValue(task.getCurrentSize(), task.getTotalSize());
    }

    /**
     * 源任务大小变更事件
     */
    private void sourceTransportTaskSizeChanged() {
        if (this.sourceTransportManager.isEmpty()) {
            this.sourceTransportBox.disappear();
            this.targetFile.loadFile();
        } else {
            this.sourceTransportBox.display();
        }
        this.updateLayout();
    }

    /**
     * 目标状态改变事件
     *
     * @param status 状态
     * @param task   任务
     */
    private void targetTransportStatusChanged(String status, SftpTransportTask task) {
        StringBuilder builder = new StringBuilder();
        builder.append(I18nHelper.task()).append(": ").append(this.targetTransportManager.getTaskSize());
        builder.append(" ").append(I18nHelper.status()).append(": ").append(status);
        builder.append(" ").append(I18nHelper.src()).append(": ").append(task.getSrcPath());
        builder.append(" ").append(I18nHelper.dest()).append(": ").append(task.getDestPath());
        this.targetFileTransport.text(builder.toString());
        this.targetTransportProgress.setValue(task.getCurrentSize(), task.getTotalSize());
    }

    /**
     * 目标监听变更事件
     *
     * @param monitor 监听器
     * @param task    任务
     */
    private void targetTransportMonitorChanged(SftpTransportMonitor monitor, SftpTransportTask task) {
        StringBuilder builder = new StringBuilder();
        builder.append(I18nHelper.task()).append(": ").append(this.targetTransportManager.getTaskSize());
//        builder.append(" ").append(I18nHelper.count()).append(": ").append(task.size());
        builder.append(" ").append(I18nHelper.speed()).append(": ").append(task.getSpeed());
        builder.append(" ").append(I18nHelper.size()).append(": ").append(task.getFileSize());
        builder.append(" ").append(I18nHelper.src()).append(": ").append(task.getSrcPath());
        builder.append(" ").append(I18nHelper.dest()).append(": ").append(task.getDestPath());
        builder.append(" ").append(I18nHelper.current()).append(": ").append(monitor.getLocalFileName());
        this.targetFileTransport.text(builder.toString());
        this.targetTransportProgress.setValue(task.getCurrentSize(), task.getTotalSize());
    }

    /**
     * 目标任务大小变更事件
     */
    private void targetTransportTaskSizeChanged() {
        if (this.targetTransportManager.isEmpty()) {
            this.targetTransportBox.disappear();
            this.sourceFile.loadFile();
        } else {
            this.targetTransportBox.display();
        }
        this.updateLayout();
    }

    /**
     * 更新布局
     */
    private synchronized void updateLayout() {
        int showNum = 0;
        if (this.sourceTransportBox.isVisible()) {
            ++showNum;
        }
        if (this.targetTransportBox.isVisible()) {
            ++showNum;
        }
        this.fileBox.setFlexHeight("100% - " + (showNum * 30));
        this.fileBox.parentAutosize();
    }

    /**
     * 隐藏来源文件
     */
    @FXML
    private void hiddenSourceFile() {
        if (!this.hiddenSourcePane.isHidden()) {
            this.hiddenSourcePane.hidden();
            this.sourceFile.setShowHiddenFile(false);
            this.hiddenSourcePane.setTipText(I18nHelper.showHiddenFiles());
        } else {
            this.hiddenSourcePane.show();
            this.sourceFile.setShowHiddenFile(true);
            this.hiddenSourcePane.setTipText(I18nHelper.doNotShowHiddenFiles());
        }
    }

    /**
     * 隐藏来源文件
     */
    @FXML
    private void hiddenTargetFile() {
        if (!this.hiddenTargetPane.isHidden()) {
            this.hiddenTargetPane.hidden();
            this.targetFile.setShowHiddenFile(false);
            this.hiddenTargetPane.setTipText(I18nHelper.showHiddenFiles());
        } else {
            this.hiddenTargetPane.show();
            this.targetFile.setShowHiddenFile(true);
            this.hiddenTargetPane.setTipText(I18nHelper.doNotShowHiddenFiles());
        }
    }

    @FXML
    private void intoSourceHome() {
        this.sourceFile.intoHome();
    }

    @FXML
    private void returnSourceDir() {
        this.sourceFile.returnDir();
    }

    @FXML
    private void intoTargetHome() {
        this.targetFile.intoHome();
    }

    @FXML
    private void returnTargetDir() {
        this.targetFile.returnDir();
    }
}
