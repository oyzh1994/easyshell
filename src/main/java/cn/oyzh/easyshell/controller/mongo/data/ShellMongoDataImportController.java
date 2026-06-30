package cn.oyzh.easyshell.controller.mongo.data;

import cn.oyzh.common.cache.CacheHelper;
import cn.oyzh.common.date.DateUtil;
import cn.oyzh.common.system.SystemUtil;
import cn.oyzh.common.thread.ThreadUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.data.db.ui.DBDataFieldSeparatorComboBox;
import cn.oyzh.easyshell.data.db.ui.DBDataRecordLabelComboBox;
import cn.oyzh.easyshell.data.mongo.dto.ShellMongoDataImportFile;
import cn.oyzh.easyshell.data.mongo.handler.ShellMongoDataImportHandler;
import cn.oyzh.easyshell.data.mongo.ui.DBDataRecordSeparatorComboBox;
import cn.oyzh.easyshell.data.mongo.ui.DBDataTxtIdentifierComboBox;
import cn.oyzh.easyshell.data.mongo.ui.ShellMongoDataImportFileTableView;
import cn.oyzh.easyshell.data.db.ui.DBDataDateTextFiled;
import cn.oyzh.easyshell.fx.mongo.ShellMongoDatabaseComboBox;
import cn.oyzh.easyshell.mongo.ShellMongoClient;
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
import cn.oyzh.fx.plus.controls.toggle.FXToggleGroup;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.node.NodeGroupUtil;
import cn.oyzh.fx.plus.util.Counter;
import cn.oyzh.fx.plus.util.FXUtil;
import cn.oyzh.fx.plus.window.FXStageStyle;
import cn.oyzh.fx.plus.window.StageAttribute;
import cn.oyzh.i18n.I18nHelper;
import javafx.fxml.FXML;
import javafx.scene.control.RadioButton;
import javafx.stage.Modality;
import javafx.stage.WindowEvent;

import java.io.File;
import java.util.Date;


/**
 * db数据导入业务
 *
 * @author oyzh
 * @since 2024/08/30
 */
@StageAttribute(
        stageStyle = FXStageStyle.EXTENDED,
        modality = Modality.APPLICATION_MODAL,
        value = FXConst.FXML_PATH + "mongo/data/shellMongoDataImport.fxml"
)
public class ShellMongoDataImportController extends StageController {

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
     * 第四步
     */
    @FXML
    private FXVBox step4;

    /**
     * 第五步
     */
    @FXML
    private FXVBox step5;

    /**
     * 导入表组件
     */
    @FXML
    private ShellMongoDataImportFileTableView importFileTableView;

    /**
     * 文件类型
     */
    @FXML
    private FXToggleGroup fileType;

    /**
     * db客户端
     */
    private ShellMongoClient dbClient;

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
     * 行标签
     */
    @FXML
    private DBDataRecordLabelComboBox recordLabel;

    /**
     * 标签属性作为表字段
     */
    @FXML
    private FXCheckBox attrToColumn;

    /**
     * 字段索引
     */
    @FXML
    private NumberTextField columnIndex;

    /**
     * 数据起始索引
     */
    @FXML
    private NumberTextField dataStartIndex;

    /**
     * 日期预览
     */
    @FXML
    private FXLabel datePreview;

    /**
     * 日期格式
     */
    @FXML
    private DBDataDateTextFiled dateFormat;

    /**
     * 记录分隔符
     */
    @FXML
    private DBDataRecordSeparatorComboBox recordSeparator;

    /**
     * 字段分割符
     */
    @FXML
    private DBDataFieldSeparatorComboBox fieldSeparator;

    /**
     * 文本识别符
     */
    @FXML
    private DBDataTxtIdentifierComboBox txtIdentifier;

    /**
     * 导入模式
     */
    @FXML
    private FXToggleGroup importMode;

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
    private ShellMongoDataImportHandler importHandler;

    /**
     * 数据库
     */
    @FXML
    private ShellMongoDatabaseComboBox database;

    /**
     * 数据库
     */
    private String dbName;

    /**
     * 执行导入
     */
    @FXML
    private void doImport() {
        // 重置参数
        this.counter.reset();
        this.importMsg.clear();
        // 开始处理
        this.importMsg.clear();
        // 生成导入处理器
        if (this.importHandler == null) {
            this.importHandler = new ShellMongoDataImportHandler(this.dbClient, this.dbName);
            this.importHandler.setMessageHandler(str -> this.importMsg.appendLine(str))
                    .setProcessedHandler(count -> {
                        if (count > 0) {
                            this.counter.incrSuccess(count);
                        } else {
                            this.counter.incrFail(Math.abs(count));
                        }
                        this.updateStatus(I18nHelper.importInProgress());
                    });
        } else {
            this.importHandler.interrupt(false);
        }
        // 文件类型
        this.importHandler.setFileType(this.fileType.selectedUserData());
        // 文件
        this.importHandler.setFiles(this.importFileTableView.getItems());
        // 字段分隔符
        this.importHandler.fieldSeparator(this.fieldSeparator.value());
        // 记录分隔符
        this.importHandler.recordSeparator(this.recordSeparator.value());
        // 文本识别符
        this.importHandler.txtIdentifier(this.txtIdentifier.getSelectedItem());
        // 日期格式
        this.importHandler.dateFormat(this.dateFormat.getTextTrim());
        // 标签属性作为表字段
        this.importHandler.attrToColumn(this.attrToColumn.isSelected());
        // 导入模式
        this.importHandler.importMode(this.importMode.selectedUserData());
        // 字段索引
        this.importHandler.columnIndex(this.columnIndex.getIntValue() - 1);
        // 数据起始位置
        this.importHandler.dataStartIndex(this.dataStartIndex.getIntValue() - 1);
        // 行记录标签
        if (!this.recordLabel.isRoot()) {
            this.importHandler.recordLabel(this.recordLabel.getSelectedItem());
        } else {
            this.importHandler.recordLabel(null);
        }
        NodeGroupUtil.disable(this.stage, "exec");
        this.stage.appendTitle("===" + I18nHelper.importInProgress() + "===");
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
            } catch (Exception ex) {
                if (ex.getClass().isAssignableFrom(InterruptedException.class)) {
                    this.updateStatus(I18nHelper.operationCancel());
                    MessageBox.okToast(I18nHelper.operationCancel());
                } else {
                    ex.printStackTrace();
                    this.updateStatus(I18nHelper.operationFail());
                    MessageBox.exception(ex);
                }
            } finally {
                // 结束处理
                NodeGroupUtil.enable(this.stage, "exec");
                this.stopImportBtn.disable();
                this.stage.restoreTitle();
                SystemUtil.gcLater();
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

    /**
     * 刷新日期预览
     */
    private void flushDatePreview() {
        try {
            String format = this.dateFormat.getTextTrim();
            this.datePreview.setText(I18nHelper.currentTime() + " " + DateUtil.format(new Date(), format));
        } catch (Exception ex) {
            this.datePreview.setText(I18nHelper.invalidFormat());
        }
    }

    @Override
    protected void bindListeners() {
        super.bindListeners();
        this.dateFormat.textProperty().addListener((observable, oldValue, newValue) -> this.flushDatePreview());
        this.database.selectedItemChanged((observable, oldValue, newValue) -> {
            this.dbName = newValue;
            this.importFileTableView.clearItems();
            CacheHelper.set("dbName", this.dbName);
        });
    }

    @Override
    public void onWindowShown(WindowEvent event) {
        this.dbName = this.getProp("dbName");
        this.dbClient = this.getProp("dbClient");
        if (StringUtil.isNotBlank(this.dbName)) {
            this.database.addItem(this.dbName);
            this.database.selectFirst();
            this.database.disable();
        } else {
            this.database.init(this.dbClient);
            this.database.enable();
        }
        CacheHelper.set("dbName", this.dbName);
        CacheHelper.set("dbClient", this.dbClient);
        this.stage.hideOnEscape();
        super.onWindowShown(event);
    }

    @Override
    public void onWindowHidden(WindowEvent event) {
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
        this.importFileTableView.clearItems();
        this.step2.disappear();
        this.step1.display();
    }

    @FXML
    private void showStep2() {
        RadioButton button = this.fileType.selectedToggle();
        if (button == null) {
            MessageBox.warn(I18nHelper.pleaseSelectType());
            return;
        }
        this.step1.disappear();
        this.step3.disappear();
        this.step2.display();
    }

    @FXML
    private void showStep3() {
        if (this.importFileTableView.isItemEmpty()) {
            MessageBox.warn(I18nHelper.pleaseSelectFile());
            return;
        }
        // 文件类型
        String type = this.fileType.selectedUserData();
        // 显示对应组件
        if (StringUtil.equalsIgnoreCase(type, "json")) {
            NodeGroupUtil.display(this.getStage(), "recordLabel");
            NodeGroupUtil.disappear(this.getStage(), "attrToColumn");
            NodeGroupUtil.disappear(this.getStage(), "txtIdentifier");
            NodeGroupUtil.disappear(this.getStage(), "fieldSeparator");
            NodeGroupUtil.disappear(this.getStage(), "recordSeparator");
            NodeGroupUtil.disappear(this.getStage(), "columnIndex");
            NodeGroupUtil.disappear(this.getStage(), "dataStartIndex");
        } else if (StringUtil.equalsIgnoreCase(type, "txt")) {
            NodeGroupUtil.display(this.getStage(), "txtIdentifier");
            NodeGroupUtil.display(this.getStage(), "fieldSeparator");
            NodeGroupUtil.display(this.getStage(), "recordSeparator");
            NodeGroupUtil.display(this.getStage(), "columnIndex");
            NodeGroupUtil.display(this.getStage(), "dataStartIndex");
            NodeGroupUtil.disappear(this.getStage(), "recordLabel");
            NodeGroupUtil.disappear(this.getStage(), "attrToColumn");
        } else if (StringUtil.equalsIgnoreCase(type, "csv")) {
            NodeGroupUtil.display(this.getStage(), "txtIdentifier");
            NodeGroupUtil.display(this.getStage(), "recordSeparator");
            NodeGroupUtil.display(this.getStage(), "columnIndex");
            NodeGroupUtil.display(this.getStage(), "dataStartIndex");
            NodeGroupUtil.disappear(this.getStage(), "fieldSeparator");
            NodeGroupUtil.disappear(this.getStage(), "recordLabel");
            NodeGroupUtil.disappear(this.getStage(), "attrToColumn");
        } else if (StringUtil.equalsIgnoreCase(type, "xml")) {
            NodeGroupUtil.display(this.getStage(), "attrToColumn");
            NodeGroupUtil.display(this.getStage(), "recordLabel");
            NodeGroupUtil.disappear(this.getStage(), "txtIdentifier");
            NodeGroupUtil.disappear(this.getStage(), "recordSeparator");
            NodeGroupUtil.disappear(this.getStage(), "columnIndex");
            NodeGroupUtil.disappear(this.getStage(), "dataStartIndex");
            NodeGroupUtil.disappear(this.getStage(), "fieldSeparator");
        } else if (StringUtil.equalsIgnoreCase(type, "excel")) {
            NodeGroupUtil.display(this.getStage(), "columnIndex");
            NodeGroupUtil.display(this.getStage(), "dataStartIndex");
            NodeGroupUtil.disappear(this.getStage(), "attrToColumn");
            NodeGroupUtil.disappear(this.getStage(), "recordLabel");
            NodeGroupUtil.disappear(this.getStage(), "txtIdentifier");
            NodeGroupUtil.disappear(this.getStage(), "recordSeparator");
            NodeGroupUtil.disappear(this.getStage(), "fieldSeparator");
        }
        this.flushDatePreview();
        this.step2.disappear();
        this.step4.disappear();
        this.step3.display();
    }

    @FXML
    private void showStep4() {
        this.step3.disappear();
        this.step5.disappear();
        this.step4.display();
    }

    @FXML
    private void showStep5() {
        this.step4.disappear();
        this.step5.display();
    }

    @FXML
    private void addFile() {
        String fileType = this.fileType.selectedUserData();
        FileExtensionFilter filter = FXChooser.extensionFilter(fileType);
        File file = FileChooserHelper.choose(I18nHelper.pleaseSelectFile(), filter);
        if (file != null) {
            ShellMongoDataImportFile importFile = new ShellMongoDataImportFile();
            importFile.setFile(file);
            this.importFileTableView.addItem(importFile);
        }
    }

    @FXML
    private void deleteFile() {
        this.importFileTableView.removeSelectedItem();
    }

}
