package cn.oyzh.easyshell.controller.mysql.data;

import cn.oyzh.common.file.FileUtil;
import cn.oyzh.common.system.SystemUtil;
import cn.oyzh.common.thread.ThreadUtil;
import cn.oyzh.easyshell.db.handler.DBDataDumpHandler;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.fx.mysql.data.DBDumpDataTypeComboBox;
import cn.oyzh.easyshell.mysql.ShellMysqlClient;
import cn.oyzh.fx.gui.text.area.MsgTextArea;
import cn.oyzh.fx.gui.text.field.ReadOnlyTextField;
import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.fx.plus.chooser.FXChooser;
import cn.oyzh.fx.plus.chooser.FileChooserHelper;
import cn.oyzh.fx.plus.chooser.FileExtensionFilter;
import cn.oyzh.fx.plus.controller.StageController;
import cn.oyzh.fx.plus.controls.box.FXVBox;
import cn.oyzh.fx.plus.controls.button.FXButton;
import cn.oyzh.fx.plus.controls.label.FXLabel;
import cn.oyzh.fx.plus.i18n.I18nResourceBundle;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.node.NodeGroupUtil;
import cn.oyzh.fx.plus.util.Counter;
import cn.oyzh.fx.plus.util.FXUtil;
import cn.oyzh.fx.plus.window.StageAdapter;
import cn.oyzh.fx.plus.window.StageAttribute;
import cn.oyzh.i18n.I18nHelper;
import javafx.fxml.FXML;
import javafx.stage.Modality;
import javafx.stage.WindowEvent;

import java.io.File;
import java.io.IOException;
import java.util.List;


/**
 * db数据转储业务
 *
 * @author oyzh
 * @since 2024/08/22
 */
@StageAttribute(
        modality = Modality.WINDOW_MODAL,
        value = FXConst.FXML_PATH + "mysql/data/shellMysqlDataDump.fxml"
)
public class ShellMysqlDataDumpController extends StageController {

    /**
     * 连接信息
     */
    private ShellConnect dbInfo;

    /**
     * db客户端
     */
    private ShellMysqlClient dbClient;

    /**
     * 1 库
     * 2 表
     */
    private int dumpType;

    /**
     * 结束转储按钮
     */
    @FXML
    private FXButton stopDumpBtn;

    /**
     * 转储状态
     */
    @FXML
    private FXLabel dumpStatus;

    /**
     * 转储消息
     */
    @FXML
    private MsgTextArea dumpMsg;

    /**
     * 连接
     */
    @FXML
    private ReadOnlyTextField connect;

    /**
     * 数据库
     */
    @FXML
    private ReadOnlyTextField database;

    /**
     * 表组件
     */
    @FXML
    private FXVBox tableBox;

    /**
     * 表
     */
    @FXML
    private ReadOnlyTextField table;

    /**
     * 数据类型
     */
    @FXML
    private DBDumpDataTypeComboBox dataType;

    /**
     * 转储操作任务
     */
    private Thread execTask;

    /**
     * 计数器
     */
    private final Counter counter = new Counter();

    /**
     * 转储文件
     */
    private File dumpFile;

    /**
     * 转储处理器
     */
    private DBDataDumpHandler dumpHandler;

    /**
     * 检查转储文件
     *
     * @return 结果
     */
    private boolean checkDumpFile() {
        if (this.dumpFile == null || !this.dumpFile.exists()) {
            String name = "";
            if (this.dumpType == 1) {
                name = this.database.getText();
            } else if (this.dumpType == 2) {
                name = this.table.getText();
            }
            if (this.dataType.isFull()) {
                name += ".full";
            } else {
                name += ".structure";
            }
            // name += ".sql";
            FileExtensionFilter filter = FXChooser.sqlExtensionFilter();
            this.dumpFile = FileChooserHelper.save(I18nHelper.saveFile(), name, List.of(filter), this.stage.stage());
            if (this.dumpFile != null) {
                FileUtil.touch(this.dumpFile);
            }
        }
        return this.dumpFile != null;
    }

    /**
     * 执行转储
     */
    @FXML
    private void doDump() throws IOException {
        // 检查转储文件
        if (!this.checkDumpFile()) {
            return;
        }
        // 重置参数
        this.counter.reset();
        this.dumpMsg.clear();
        // 开始处理
        this.dumpMsg.clear();
        // 生成转储处理器
        if (this.dumpHandler == null) {
            this.dumpHandler = DBDataDumpHandler.newHandler(this.dbClient, this.database.getText());
            this.dumpHandler.setDbInfo(this.dbInfo)
                    .setQueryLimit(10_000)
                    .setMessageHandler(str -> this.dumpMsg.appendLine(str))
                    .setProcessedHandler(count -> {
                        if (count > 0) {
                            this.counter.incrSuccess(count);
                        } else {
                            this.counter.incrFail(Math.abs(count));
                        }
                        this.updateStatus(I18nHelper.dumpInProgress());
                    });
        } else {
            this.dumpHandler.interrupt(false);
        }
        // 设置参数
        this.dumpHandler.dumpFile(this.dumpFile)
                .setTableName(this.table.getText())
                .setDumpType((byte) this.dumpType)
                .setDataType((byte) this.dataType.getSelectedIndex());
        NodeGroupUtil.disable(this.stage, "exec");
        this.stage.appendTitle("===" + I18nHelper.dumpProcessing() + "===");
        // 执行转储
        this.execTask = ThreadUtil.start(() -> {
            try {
                this.stopDumpBtn.enable();
                // 更新状态
                this.updateStatus(I18nHelper.dumpStarting());
                // 执行转储
                this.dumpHandler.doDump();
                // 更新状态
                this.updateStatus(I18nHelper.dumpFinished());
            } catch (Exception e) {
                if (e.getClass().isAssignableFrom(InterruptedException.class)) {
                    this.updateStatus(I18nHelper.operationCancel());
                    MessageBox.okToast(I18nHelper.operationCancel());
                } else {
                    e.printStackTrace();
                    this.updateStatus(I18nHelper.operationFail());
                    MessageBox.warn(I18nHelper.operationFail());
                }
            } finally {
                // 结束处理
                NodeGroupUtil.enable(this.stage, "exec");
                this.stopDumpBtn.disable();
                this.stage.restoreTitle();
                SystemUtil.gcLater();
            }
        });
    }

    /**
     * 结束转储
     */
    @FXML
    private void stopDump() {
        ThreadUtil.interrupt(this.execTask);
        this.execTask = null;
        if (this.dumpHandler != null) {
            this.dumpHandler.interrupt();
        }
    }

    @Override
    public void onWindowShown(WindowEvent event) {
        super.onWindowShown(event);
        this.dbClient = this.getProp("dbClient");
        this.dumpType = this.getProp("dumpType");
        this.dbInfo = this.dbClient.getShellConnect();
        String dbName = this.getProp("dbName");
        String tableName = this.getProp("tableName");
        this.database.setText(dbName);
        this.connect.setText(this.dbInfo.getName());
        if (this.dumpType == 2) {
            this.table.setText(tableName);
            this.tableBox.display();
        }
        this.stage.hideOnEscape();
    }

    @Override
    public void onWindowHidden(WindowEvent event) {
        super.onWindowHidden(event);
        this.stopDump();
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
        FXUtil.runLater(() -> this.dumpStatus.setText(this.counter.unknownFormat()));
    }

    @Override
    public String getViewTitle() {
        return I18nResourceBundle.i18nString("base.title.dump");
    }

    @Override
    public void onStageInitialize(StageAdapter stage) {
        super.onStageInitialize(stage);
        this.tableBox.managedBindVisible();
    }

    @Override
    protected void bindListeners() {
        super.bindListeners();
        this.dataType.selectedItemChanged((observableValue, s, t1) -> {
            this.dumpFile = null;
        });
    }
}
