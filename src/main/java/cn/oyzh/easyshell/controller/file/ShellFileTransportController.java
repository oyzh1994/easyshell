package cn.oyzh.easyshell.controller.file;

import cn.oyzh.common.util.CollectionUtil;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.file.ShellFile;
import cn.oyzh.easyshell.file.ShellFileClient;
import cn.oyzh.easyshell.file.ShellFileTransportTask;
import cn.oyzh.easyshell.fx.file.ShellFileConnectComboBox;
import cn.oyzh.easyshell.fx.file.ShellFileLocationTextField;
import cn.oyzh.easyshell.fx.file.ShellFileTransportFileTableView;
import cn.oyzh.easyshell.fx.file.ShellFileTransportTaskTableView;
import cn.oyzh.easyshell.util.ShellClientUtil;
import cn.oyzh.easyshell.util.ShellI18nHelper;
import cn.oyzh.fx.gui.svg.pane.HiddenSVGPane;
import cn.oyzh.fx.gui.text.field.ClearableTextField;
import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.fx.plus.controller.StageController;
import cn.oyzh.fx.plus.controls.box.FXHBox;
import cn.oyzh.fx.plus.controls.box.FXVBox;
import cn.oyzh.fx.plus.controls.label.FXLabel;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.validator.ValidatorUtil;
import cn.oyzh.fx.plus.window.StageAttribute;
import cn.oyzh.fx.plus.window.StageManager;
import cn.oyzh.i18n.I18nHelper;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
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
        value = FXConst.FXML_PATH + "file/shellFileTransport.fxml"
)
public class ShellFileTransportController extends StageController {

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
    private ShellFileConnectComboBox sourceInfo;

    /**
     * 目标信息
     */
    @FXML
    private ShellFileConnectComboBox targetInfo;

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
    private ShellFileTransportFileTableView sourceFile;

    /**
     * 目标文件
     */
    @FXML
    private ShellFileTransportFileTableView targetFile;

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
    private ShellFileLocationTextField sourceLocation;

    /**
     * 目标当前位置
     */
    @FXML
    private ShellFileLocationTextField targetLocation;

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
    private ShellFileClient sourceClient;

    /**
     * 目标客户端
     */
    private ShellFileClient targetClient;

    /**
     * 文件传输列表
     */
    @FXML
    private ShellFileTransportTaskTableView transportTable;

    /**
     * 执行传输1
     */
    private void doTransport1(List<ShellFile> files) {
        try {
            if (CollectionUtil.isEmpty(files)) {
                return;
            }
            // 检查文件是否存在
            for (ShellFile file : files) {
                if (file.isCurrentFile() || file.isReturnDirectory()) {
                    continue;
                }
                if (this.targetFile.existFile(file.getFileName()) && !MessageBox.confirm("[" + file.getFileName() + "] " + ShellI18nHelper.fileTip4())) {
                    return;
                }
            }
            String remotePath = this.targetFile.getLocation();
            this.doTransport(files, remotePath, this.sourceClient, this.targetClient);
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }

    /**
     * 执行传输2
     */
    private void doTransport2(List<ShellFile> files) {
        try {
            if (CollectionUtil.isEmpty(files)) {
                return;
            }
            // 检查文件是否存在
            for (ShellFile file : files) {
                if (file.isCurrentFile() || file.isReturnDirectory()) {
                    continue;
                }
                if (this.sourceFile.existFile(file.getFileName()) && !MessageBox.confirm("[" + file.getFileName() + "] " + ShellI18nHelper.fileTip4())) {
                    return;
                }
            }
            String remotePath = this.sourceFile.getLocation();
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
    private void doTransport(List<ShellFile> files, String remotePath, ShellFileClient sourceClient, ShellFileClient targetClient) throws Exception {
        for (ShellFile file : files) {
            if (!file.isNormal()) {
                continue;
            }
            sourceClient.doTransport(remotePath, file, targetClient);
        }
    }

    @Override
    protected void bindListeners() {
        super.bindListeners();
        this.sourceInfo.selectedItemChanged((observable, oldValue, newValue) -> {
            if (newValue != null) {
                // 初始化名称
                this.sourceHost.setText(newValue.getHost());
                this.sourceInfoName.setText(newValue.getName());
            } else {
                this.sourceHost.clear();
                this.sourceInfoName.clear();
            }
            if (this.sourceClient != null) {
                try {
                    this.sourceClient.close();
                    this.sourceClient = null;
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
        this.targetInfo.selectedItemChanged((observable, oldValue, newValue) -> {
            if (newValue != null) {
                // 初始化名称
                this.targetHost.setText(newValue.getHost());
                this.targetInfoName.setText(newValue.getName());
            } else {
                this.targetHost.clear();
                this.targetInfoName.clear();
            }
            if (this.targetClient != null) {
                try {
                    this.targetClient.close();
                    this.targetClient = null;
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
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
                ValidatorUtil.validFail(this.sourceInfo);
                return;
            }
            // 检查目标连接
            if (targetInfo == null) {
                ValidatorUtil.validFail(this.targetInfo);
                return;
            }
            // 检查连接是否一样
            if (sourceInfo.compare(targetInfo)) {
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

    /**
     * 初始化文件表格
     */
    protected void initFileTable() {
        // 设置客户端
        this.sourceFile.setClient(this.sourceClient);
        this.targetFile.setClient(this.targetClient);
        // 处理传输列表
        ObservableList<ShellFileTransportTask> transportTasks = this.transportTable.getItems();
        // 列表监听
        ListChangeListener<ShellFileTransportTask> taskListChangeListener = change -> {
            transportTasks.clear();
            transportTasks.addAll(this.sourceClient.transportTasks());
            transportTasks.addAll(this.targetClient.transportTasks());
            if (this.targetClient.isTransportTaskEmpty()) {
                this.targetFile.loadFile();
            }
            if (this.sourceClient.isTransportTaskEmpty()) {
                this.sourceFile.loadFile();
            }
        };
        // 监听目标传输
        this.sourceClient.transportTasks().addListener(taskListChangeListener);
        this.targetClient.transportTasks().addListener(taskListChangeListener);
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
        if (!this.transportTable.isItemEmpty() && !MessageBox.confirm(ShellI18nHelper.fileTip18())) {
            event.consume();
            return;
        }
        // 取消任务
        this.transportTable.cancel();
        // 关闭连接
        if (this.sourceClient != null) {
            try {
                this.sourceClient.close();
                this.sourceClient = null;
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        if (this.targetClient != null) {
            try {
                this.targetClient.close();
                this.targetClient = null;
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        super.onWindowCloseRequest(event);
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
        try {
            this.sourceFile.intoHome();
        } catch (Exception ex) {
           MessageBox.exception(ex);
        }
    }

    @FXML
    private void returnSourceDir() {
        this.sourceFile.returnDir();
    }

    @FXML
    private void intoTargetHome() {
        try {
            this.targetFile.intoHome();
        } catch (Exception ex) {
           MessageBox.exception(ex);
        }
    }

    @FXML
    private void returnTargetDir() {
        this.targetFile.returnDir();
    }
}
