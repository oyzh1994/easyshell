package cn.oyzh.easyshell.controller.mysql.data;

import cn.oyzh.common.system.SystemUtil;
import cn.oyzh.common.thread.ThreadUtil;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.handler.mysql.DataRunSqlFileHandler;
import cn.oyzh.easyshell.mysql.MysqlClient;
import cn.oyzh.fx.gui.text.area.MsgTextArea;
import cn.oyzh.fx.gui.text.field.ChooseFileTextField;
import cn.oyzh.fx.gui.text.field.ReadOnlyTextField;
import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.fx.plus.chooser.FXChooser;
import cn.oyzh.fx.plus.controller.StageController;
import cn.oyzh.fx.plus.controls.button.FXButton;
import cn.oyzh.fx.plus.controls.button.FXCheckBox;
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


/**
 * db运行sql业务
 *
 * @author oyzh
 * @since 2024/08/29
 */
@StageAttribute(
        modality = Modality.WINDOW_MODAL,
        value = FXConst.FXML_PATH + "mysql/data/shellMysqlRunSqlFile.fxml"
)
public class MysqlRunSqlFileController extends StageController {

    /**
     * 连接信息
     */
    private ShellConnect dbInfo;

    /**
     * db客户端
     */
    private MysqlClient dbClient;

    /**
     * 结束运行sql按钮
     */
    @FXML
    private FXButton stopSqlFileBtn;

    /**
     * 执行状态
     */
    @FXML
    private FXLabel execStatus;

    /**
     * 执行消息
     */
    @FXML
    private MsgTextArea execMsg;

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
     * 遇到错误时继续
     */
    @FXML
    private FXCheckBox continueWithErrors;

    /**
     * 文件
     */
    @FXML
    private ChooseFileTextField file;

    /**
     * sql操作任务
     */
    private Thread execTask;

    /**
     * 计数器
     */
    private final Counter counter = new Counter();

    /**
     * sql处理器
     */
    private DataRunSqlFileHandler sqlFileHandler;

    /**
     * 检查sql文件
     *
     * @return 结果
     */
    private boolean checkSqlFile() {
        File sqlFile = this.file.getFile();
        if (sqlFile == null) {
            MessageBox.warn(I18nHelper.pleaseSelectFile());
            return false;
        }
        return true;
    }

    /**
     * 执行sql
     */
    @FXML
    private void runSqlFile() throws IOException {
        // 检查sql文件
        if (!this.checkSqlFile()) {
            return;
        }
        // 重置参数
        this.counter.reset();
        this.execMsg.clear();
        // 开始处理
        this.execMsg.clear();
        // 生成sql处理器
        if (this.sqlFileHandler == null) {
            this.sqlFileHandler = DataRunSqlFileHandler.newHandler(this.dbClient, this.database.getText());
            this.sqlFileHandler.setDbInfo(this.dbInfo)
                    .setMessageHandler(str -> this.execMsg.appendLine(str))
                    .setProcessedHandler(count -> {
                        if (count > 0) {
                            this.counter.incrSuccess(count);
                        } else {
                            this.counter.incrFail(Math.abs(count));
                        }
                        this.updateStatus(I18nHelper.execInProgress());
                    });
        } else {
            this.sqlFileHandler.interrupt(false);
        }
        // 设置参数
        this.sqlFileHandler.sqlFile(this.file.getFile())
                .setContinueWithErrors(this.continueWithErrors.isSelected());
        NodeGroupUtil.disable(this.stage, "exec");
        this.stage.appendTitle("===" + I18nHelper.execProcessing() + "===");
        // 执行sql
        this.execTask = ThreadUtil.start(() -> {
            try {
                this.stopSqlFileBtn.enable();
                // 更新状态
                this.updateStatus(I18nHelper.execStarting());
                // 执行sql
                this.sqlFileHandler.runSqlFile();
                // 更新状态
                this.updateStatus(I18nHelper.execFinished());
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
                this.stopSqlFileBtn.disable();
                this.stage.restoreTitle();
                SystemUtil.gcLater();
            }
        });
    }

    /**
     * 结束sql
     */
    @FXML
    private void stopSqlFile() {
        ThreadUtil.interrupt(this.execTask);
        this.execTask = null;
        if (this.sqlFileHandler != null) {
            this.sqlFileHandler.interrupt();
        }
    }

    @Override
    public void onWindowShown(WindowEvent event) {
        super.onWindowShown(event);
        this.dbInfo = this.getProp("dbInfo");
        this.dbClient = this.getProp("dbClient");
        String dbName = this.getProp("dbName");
        this.database.setText(dbName);
        this.connect.setText(this.dbInfo.getName());
        this.stage.hideOnEscape();
    }

    @Override
    public void onWindowHidden(WindowEvent event) {
        super.onWindowHidden(event);
        this.stopSqlFile();
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
        FXUtil.runLater(() -> this.execStatus.setText(this.counter.unknownFormat()));
    }

    @Override
    public String getViewTitle() {
        return I18nResourceBundle.i18nString("base.runSqlFile");
    }

    @Override
    public void onStageInitialize(StageAdapter stage) {
        super.onStageInitialize(stage);
        this.file.setFilter(FXChooser.sqlExtensionFilter());
    }
}
