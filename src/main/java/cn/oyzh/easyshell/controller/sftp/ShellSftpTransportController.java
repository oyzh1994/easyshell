package cn.oyzh.easyshell.controller.sftp;

import cn.oyzh.common.thread.DownLatch;
import cn.oyzh.common.thread.ThreadUtil;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.fx.ShellConnectComboBox;
import cn.oyzh.easyshell.shell.ShellClient;
import cn.oyzh.easyshell.shell.ShellClientUtil;
import cn.oyzh.fx.gui.combobox.CharsetComboBox;
import cn.oyzh.fx.gui.text.area.MsgTextArea;
import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.fx.plus.controller.StageController;
import cn.oyzh.fx.plus.controls.box.FXVBox;
import cn.oyzh.fx.plus.controls.button.FXButton;
import cn.oyzh.fx.plus.controls.button.FXCheckBox;
import cn.oyzh.fx.plus.controls.label.FXLabel;
import cn.oyzh.fx.plus.controls.toggle.FXToggleGroup;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.util.Counter;
import cn.oyzh.fx.plus.util.FXUtil;
import cn.oyzh.fx.plus.window.FXStageStyle;
import cn.oyzh.fx.plus.window.StageAttribute;
import cn.oyzh.i18n.I18nHelper;
import javafx.fxml.FXML;
import javafx.stage.Modality;
import javafx.stage.WindowEvent;


/**
 * db数据传输业务
 *
 * @author oyzh
 * @since 2024/09/05
 */
@StageAttribute(
        stageStyle = FXStageStyle.UNIFIED,
        modality = Modality.APPLICATION_MODAL,
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
     * 第三步
     */
    @FXML
    private FXVBox step3;

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
     * 来源客户端
     */
    private ShellClient sourceClient;

    /**
     * 目标客户端
     */
    private ShellClient targetClient;

    /**
     * 结束传输按钮
     */
    @FXML
    private FXButton stopTransportBtn;

    /**
     * 传输状态
     */
    @FXML
    private FXLabel transportStatus;

    /**
     * 传输消息
     */
    @FXML
    private MsgTextArea transportMsg;

    /**
     * 节点存在时处理策略
     */
    @FXML
    private FXToggleGroup existsPolicy;

    /**
     * 适用过滤配置
     */
    @FXML
    private FXCheckBox applyFilter;

    /**
     * 传输操作任务
     */
    private Thread execTask;

    /**
     * 计数器
     */
    private final Counter counter = new Counter();

    /**
     * 执行传输
     */
    @FXML
    private void doTransport() {
        // 重置参数
        this.counter.reset();
        // 清理信息
        this.transportMsg.clear();
        this.transportStatus.clear();
    }

    /**
     * 结束传输
     */
    @FXML
    private void stopTransport() {
        ThreadUtil.interrupt(this.execTask);
        this.execTask = null;
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
    public void onWindowHidden(WindowEvent event) {
        super.onWindowHidden(event);
        this.stopTransport();
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
        FXUtil.runLater(() -> this.transportStatus.setText(this.counter.unknownFormat()));
    }

    @Override
    public String getViewTitle() {
        return I18nHelper.transportTitle();
    }

    // @Override
    // public void onStageInitialize(StageAdapter stage) {
    //     super.onStageInitialize(stage);
    //     this.step1.managedBindVisible();
    //     this.step2.managedBindVisible();
    //     this.step3.managedBindVisible();
    // }

    @FXML
    private void showStep1() {
        this.step2.disappear();
        this.step1.display();
    }

    @FXML
    private void showStep2() {
        try {
            ShellConnect sourceInfo = this.sourceInfo.getSelectedItem();
            ShellConnect targetInfo = this.targetInfo.getSelectedItem();
            if (sourceInfo == null) {
                this.sourceInfo.requestFocus();
                MessageBox.warn(I18nHelper.pleaseSelectSourceConnect());
                return;
            }
            if (targetInfo == null) {
                this.targetInfo.requestFocus();
                MessageBox.warn(I18nHelper.pleaseSelectTargetConnect());
                return;
            }

            if (sourceInfo.compare(targetInfo)) {
                this.sourceInfo.requestFocus();
                MessageBox.warn(I18nHelper.connectionsCannotBeTheSame());
                return;
            }

            this.getStage().appendTitle("===" + I18nHelper.connectIng() + "===");
            this.getStage().disable();

            if (this.sourceClient == null || this.sourceClient.isClosed()) {
                DownLatch latch = DownLatch.of();
                ThreadUtil.start(() -> {
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

            if (this.targetClient == null || this.targetClient.isClosed()) {
                DownLatch latch = DownLatch.of();
                ThreadUtil.start(() -> {
                    try {
                        this.targetClient = ShellClientUtil.newClient(targetInfo);
                        this.targetClient.start(2500);
                    } finally {
                        latch.countDown();
                    }
                });
                if (!latch.await(3000) || !this.targetClient.isConnected()) {
                    this.targetClient.close();
                    this.targetClient = null;
                    this.targetInfo.requestFocus();
                    MessageBox.warn(targetInfo.getName() + " " + I18nHelper.connectFail());
                    return;
                }
            }

            this.step1.disappear();
            this.step3.disappear();
            this.step2.display();
        } finally {
            this.getStage().restoreTitle();
            this.getStage().enable();
        }
    }

    @FXML
    private void showStep3() {
        this.step2.disappear();
        this.step3.display();
    }
}
