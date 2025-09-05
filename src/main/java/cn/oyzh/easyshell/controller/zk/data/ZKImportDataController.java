package cn.oyzh.easyshell.controller.zk.data;

import cn.oyzh.common.thread.DownLatch;
import cn.oyzh.common.thread.ThreadUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.handler.zk.ZKDataImportHandler;
import cn.oyzh.easyshell.zk.ZKClient;
import cn.oyzh.easyshell.zk.ZKClientUtil;
import cn.oyzh.fx.gui.combobox.CharsetComboBox;
import cn.oyzh.fx.gui.text.area.MsgTextArea;
import cn.oyzh.fx.gui.text.field.NumberTextField;
import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.fx.plus.chooser.FXChooser;
import cn.oyzh.fx.plus.chooser.FileChooserHelper;
import cn.oyzh.fx.plus.chooser.FileExtensionFilter;
import cn.oyzh.fx.plus.controller.StageController;
import cn.oyzh.fx.plus.controls.box.FXVBox;
import cn.oyzh.fx.plus.controls.button.FXButton;
import cn.oyzh.fx.plus.controls.button.FXCheckBox;
import cn.oyzh.fx.plus.controls.label.FXLabel;
import cn.oyzh.fx.plus.controls.text.FXText;
import cn.oyzh.fx.plus.controls.toggle.FXToggleGroup;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.node.NodeGroupUtil;
import cn.oyzh.fx.plus.tray.TrayManager;
import cn.oyzh.fx.plus.util.Counter;
import cn.oyzh.fx.plus.util.FXUtil;
import cn.oyzh.fx.plus.window.FXStageStyle;
import cn.oyzh.fx.plus.window.StageAttribute;
import cn.oyzh.i18n.I18nHelper;
import javafx.fxml.FXML;
import javafx.stage.Modality;
import javafx.stage.WindowEvent;

import java.io.File;


/**
 * zk数据导入业务
 *
 * @author oyzh
 * @since 2024/11/28
 */
@StageAttribute(
        stageStyle = FXStageStyle.UNIFIED,
        modality = Modality.APPLICATION_MODAL,
        value = FXConst.FXML_PATH + "zk/data/zkImportData.fxml"
)
public class ZKImportDataController extends StageController {

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
     * 第三步
     */
    @FXML
    private FXVBox step3;

    // /**
    //  * 第四步
    //  */
    // @FXML
    // private FXVBox step4;

    /**
     * 导入文件
     */
    private File importFile;

    /**
     * 包含acl
     */
    @FXML
    private FXCheckBox includeACL;

    /**
     * 文件格式
     */
    @FXML
    private FXToggleGroup format;

    /**
     * 文件名
     */
    @FXML
    private FXText fileName;

    // /**
    //  * 连接名
    //  */
    // @FXML
    // private FXText connectionName;

    /**
     * 字符集
     */
    @FXML
    private CharsetComboBox charset;

    /**
     * 存在时忽略
     */
    @FXML
    private FXCheckBox ignoreExist;

    /**
     * 数据行开始
     */
    @FXML
    private NumberTextField dataRowStarts;

//    /**
//     * 选择文件
//     */
//    @FXML
//    private FXButton selectFile;

    /**
     * 结束导入按钮
     */
    @FXML
    private FXButton stopImportBtn;

    /**
     * 导入状态
     */
    @FXML
    private FXLabel importStatus;

    /**
     * 导入消息
     */
    @FXML
    private MsgTextArea importMsg;

    /**
     * 当前zk对象
     */
    private ShellConnect connect;

    /**
     * 当前zk客户端
     */
    private ZKClient client;

    /**
     * 导入操作任务
     */
    private Thread execTask;

    /**
     * 计数器
     */
    private final Counter counter = new Counter();

    /**
     * 导入处理器
     */
    private ZKDataImportHandler importHandler;

    /**
     * 执行导入
     */
    @FXML
    private void doImport() {
        // 重置参数
        this.counter.reset();
        this.importMsg.clear();
        this.importStatus.clear();
        NodeGroupUtil.disable(this.stage, "exec");
        this.stage.appendTitle("===" + I18nHelper.importProcessing() + "===");
        // 生成迁移处理器
        if (this.importHandler == null) {
            this.importHandler = new ZKDataImportHandler();
            this.importHandler.setMessageHandler(str -> this.importMsg.appendLine(str));
            this.importHandler.setProcessedHandler(count -> {
                if (count == 0) {
                    this.counter.updateIgnore();
                } else if (count < 0) {
                    this.counter.incrFail(count);
                } else {
                    this.counter.incrSuccess(count);
                }
                this.updateStatus(I18nHelper.importInProgress());
            });
        } else {
            this.importHandler.interrupt(false);
        }
        String fileType = this.format.selectedUserData();
        // 文件类型
        this.importHandler.setFileType(fileType);
        // 客户端
        this.importHandler.setClient(this.client);
        // 包含acl
        this.importHandler.setIncludeACL(this.includeACL.isSelected());
        // 存在时忽略
        this.importHandler.setIgnoreExist(this.ignoreExist.isSelected());
        // 导入文件
        this.importHandler.filePath(this.importFile.getPath());
        // 字符集
        this.importHandler.charset(this.charset.getCharsetName());
        // 数据行开始
        if (this.dataRowStarts.isEnable()) {
            this.importHandler.dataRowStarts(this.dataRowStarts.getIntValue());
        } else {
            this.importHandler.dataRowStarts(null);
        }
        // 执行导入
        this.execTask = ThreadUtil.start(() -> {
            try {
                this.stopImportBtn.enable();
                // 更新状态
                this.updateStatus(I18nHelper.importStarting());
                // 执行导入
                this.importHandler.doImport();
                // 更新状态
                this.updateStatus(I18nHelper.importFinished());
                // 发送消息到托盘
                if (!this.stage.isFocused()) {
                    TrayManager.displayInfoMessage(I18nHelper.tips(), I18nHelper.importFinished());
                }
            } catch (Exception e) {
                if (e.getClass().isAssignableFrom(InterruptedException.class)) {
                    this.updateStatus(I18nHelper.operationCancel());
                    MessageBox.warn(I18nHelper.operationCancel());
                } else {
                    e.printStackTrace();
                    this.updateStatus(I18nHelper.operationFail());
                    MessageBox.warn(I18nHelper.operationFail());
                }
            } finally {
                NodeGroupUtil.enable(this.stage, "exec");
                this.stopImportBtn.disable();
                this.stage.restoreTitle();
            }
        });
    }

    /**
     * 结束导入
     */
    @FXML
    private void stopImport() {
        ThreadUtil.interrupt(this.execTask);
        this.execTask = null;
        if (this.importHandler != null) {
            this.importHandler.interrupt();
        }
    }

    @Override
    protected void bindListeners() {
        super.bindListeners();
        this.stage.hideOnEscape();
        // 格式选择监听
        this.format.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            this.importFile = null;
            this.fileName.clear();
        });
    }

    @Override
    public void onWindowHidden(WindowEvent event) {
        super.onWindowHidden(event);
        this.stopImport();
    }

    /**
     * 更新状态
     *
     * @param extraMsg 额外信息
     */
    private void updateStatus(String extraMsg) {
        if (extraMsg != null) {
            this.counter.setExtraMsg(extraMsg);
        }
        FXUtil.runLater(() -> this.importStatus.setText(this.counter.unknownFormat()));
    }

    @Override
    public String getViewTitle() {
        return I18nHelper.importTitle();
    }

    @FXML
    private void showStep1() {
        this.step2.disappear();
        this.step3.disappear();
        // this.step4.disappear();
        this.step1.display();
    }

    @FXML
    private void showStep2() {
        this.step1.disappear();
        this.step3.disappear();
        // this.step4.disappear();
        String fileType = this.format.selectedUserData();
        // 检查是否支持跳行
        if (StringUtil.equalsAny(fileType, "excel", "csv")) {
            this.dataRowStarts.enable();
        } else {
            this.dataRowStarts.disable();
        }
        this.step2.display();
    }

    // @FXML
    // private void showStep3() {
    //     if (this.importFile == null) {
    //         this.selectFile.requestFocus();
    //         MessageBox.warn(I18nHelper.pleaseSelectFile());
    //         return;
    //     }
    //     this.step1.disappear();
    //     this.step2.disappear();
    //     this.step4.disappear();
    //     this.step3.display();
    // }

    @FXML
    private void showStep3() {
        // 检查客户端
        if (this.doConnect()) {
            this.step1.disappear();
            this.step2.disappear();
            // this.step3.disappear();
            this.step3.display();
        }
    }

    /**
     * 执行连接
     *
     * @return 结果
     */
    private boolean doConnect() {
        try {
            // 检查客户端
            if (this.client == null || this.client.isClosed()) {
                this.getStage().appendTitle("===" + I18nHelper.connectIng() + "===");
                this.getStage().disable();
                DownLatch latch = DownLatch.of();
                ThreadUtil.start(() -> {
                    try {
                        this.client = ZKClientUtil.newClient(this.connect);
                        this.client.start(2500);
                    } finally {
                        latch.countDown();
                    }
                });
                if (!latch.await(3000) || !this.client.isConnected()) {
                    this.client.close();
                    this.client = null;
                    MessageBox.warn(I18nHelper.connectInitFail());
                    return false;
                }
            }
            return true;
        } finally {
            this.getStage().restoreTitle();
            this.getStage().enable();
        }
    }

    /**
     * 选择文件
     */
    @FXML
    private void selectFile() {
        String fileType = this.format.selectedUserData();
        FileExtensionFilter filter = FXChooser.extensionFilter(fileType);
        this.importFile = FileChooserHelper.choose(I18nHelper.pleaseSelectFile(), filter);
        if (this.importFile != null) {
            this.fileName.setText(this.importFile.getPath());
        } else {
            this.fileName.clear();
        }
    }


    @Override
    public void onWindowShown(WindowEvent event) {
        super.onWindowShown(event);
        this.connect = this.getProp("connect");
        // this.connectionName.setText(this.connect.getName());
    }

    // /**
    //  * 打开旧版
    //  */
    // @FXML
    // private void openOld() {
    //     if (this.doConnect()) {
    //         this.closeWindow();
    //         StageAdapter adapter = StageManager.parseStage(ZKNodeImportController.class);
    //         adapter.setProp("zkClient", this.client);
    //         adapter.display();
    //     }
    // }
}
