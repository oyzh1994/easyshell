package cn.oyzh.easyshell.controller.sqlite.data;

import cn.oyzh.common.date.DateUtil;
import cn.oyzh.common.system.SystemUtil;
import cn.oyzh.common.thread.ThreadUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.data.sqlite.dto.SQLiteDataImportFile;
import cn.oyzh.easyshell.data.sqlite.handler.SQLiteDataImportHandler;
import cn.oyzh.easyshell.data.sqlite.ui.SQLiteDataImportFileTableView;
import cn.oyzh.easyshell.sqlite.ShellSqliteClient;
import cn.oyzh.fx.db.data.ui.DBDataDateTextFiled;
import cn.oyzh.fx.db.data.ui.DBDataFieldSeparatorComboBox;
import cn.oyzh.fx.db.data.ui.DBDataRecordLabelComboBox;
import cn.oyzh.fx.db.data.ui.DBDataRecordSeparatorComboBox;
import cn.oyzh.fx.db.data.ui.DBDataTxtIdentifierComboBox;
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
import cn.oyzh.fx.plus.window.FXStageStyle;
import cn.oyzh.fx.plus.window.StageAttribute;
import cn.oyzh.i18n.I18nHelper;
import javafx.fxml.FXML;
import javafx.scene.control.RadioButton;
import javafx.stage.WindowEvent;

import java.io.File;
import java.util.Date;

/**
 * db数据导入业务
 *
 * @author oyzh
 * @since 2026-11-02
 */
@StageAttribute(
        multipliable = true,
        stageStyle = FXStageStyle.EXTENDED,
        value = FXConst.FXML_PATH + "sqlite/data/shellSqliteDataImport.fxml"
)
public class ShellSqliteDataImportController extends StageController {

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
    private SQLiteDataImportFileTableView importFileTableView;

    /**
     * 文件类型
     */
    @FXML
    private FXToggleGroup fileType;

    /**
     * db客户端
     */
    private ShellSqliteClient dbClient;

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
     * 遇到错误时继续
     */
    @FXML
    private FXCheckBox continueWithErrors;

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
    private SQLiteDataImportHandler importHandler;

    /**
     * 执行导入
     */
    @FXML
    private void doImport() {
        // 重置参数
        this.counter.reset();
        // 开始处理
        this.importMsg.clear();
        // 生成导入处理器
        if (this.importHandler == null) {
            this.importHandler = new SQLiteDataImportHandler(this.dbClient, "main");
            this.importHandler.setMessageHandler(str -> this.importMsg.appendLine(str))
                    .setProcessedHandler(count -> {
                        this.counter.incr(count);
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
        // 遇到错误时继续
        this.importHandler.setContinueWithErrors(this.continueWithErrors.isSelected());
        // 导入模式
        this.importHandler.importMode(this.importMode.selectedUserData());
        // 字段索引
        this.importHandler.columnIndex(this.columnIndex.getIntValue() - 1);
        // 数据起始位置
        this.importHandler.dataRowStarts(this.dataStartIndex.getIntValue());
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
                if (ex instanceof InterruptedException) {
                    this.updateStatus(I18nHelper.operationCancel());
                    MessageBox.okToast(I18nHelper.operationCancel(), this.getWindow());
                } else {
                    ex.printStackTrace();
                    this.updateStatus(I18nHelper.operationFail());
                    MessageBox.exception(ex, this.getWindow());
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
    }

    @Override
    public void onWindowShown(WindowEvent event) {
        this.dbClient = this.getProp("dbClient");
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
        this.importStatus.text(this.counter.unknownFormat());
    }

    @Override
    public String getViewTitle() {
        return I18nHelper.importTitle();
    }

    /**
     * 显示第一步
     */
    @FXML
    private void showStep1() {
        this.importFileTableView.clearItems();
        this.step2.disappear();
        this.step1.display();
    }

    /**
     * 显示第二步
     */
    @FXML
    private void showStep2() {
        RadioButton button = this.fileType.selectedToggle();
        if (button == null) {
            MessageBox.warn(I18nHelper.pleaseSelectType(), this.getWindow());
            return;
        }
        this.step1.disappear();
        this.step3.disappear();
        this.step2.display();
    }

    /**
     * 显示第三步
     */
    @FXML
    private void showStep3() {
        if (this.importFileTableView.isItemEmpty()) {
            MessageBox.warn(I18nHelper.pleaseSelectFile(), this.getWindow());
            return;
        }
        for (SQLiteDataImportFile file : this.importFileTableView.getItems()) {
            file.setClient(this.dbClient);
            if (file.getFile() == null) {
                MessageBox.warn(I18nHelper.pleaseSelectFile(), this.getWindow());
                return;
            }
        }
        this.showFormatStep();
        this.step2.disappear();
        this.step4.disappear();
        this.step3.display();
    }

    /**
     * 显示第四步
     */
    @FXML
    private void showStep4() {
        this.step3.disappear();
        this.step5.disappear();
        this.step4.display();
        this.importMsg.clear();
    }

    /**
     * 显示第五步
     */
    @FXML
    private void showStep5() {
        this.step4.disappear();
        this.step5.display();
    }

    /**
     * 根据文件类型显示格式配置
     */
    private void showFormatStep() {
        String type = String.valueOf(this.fileType.selectedUserData());
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
        } else if (StringUtil.equalsAnyIgnoreCase(type, "excel", "xls", "xlsx")) {
            NodeGroupUtil.display(this.getStage(), "columnIndex");
            NodeGroupUtil.display(this.getStage(), "dataStartIndex");
            NodeGroupUtil.disappear(this.getStage(), "attrToColumn");
            NodeGroupUtil.disappear(this.getStage(), "recordLabel");
            NodeGroupUtil.disappear(this.getStage(), "txtIdentifier");
            NodeGroupUtil.disappear(this.getStage(), "recordSeparator");
            NodeGroupUtil.disappear(this.getStage(), "fieldSeparator");
        }
        this.flushDatePreview();
    }

    /**
     * 添加文件
     */
    @FXML
    private void addFile() {
        String fileType = this.fileType.selectedUserData();
        if (StringUtil.isBlank(fileType)) {
            MessageBox.warn(I18nHelper.pleaseSelectType(), this.getWindow());
            return;
        }
        FileExtensionFilter filter = FXChooser.extensionFilter(fileType);
        File selected = FileChooserHelper.choose(I18nHelper.pleaseSelectFile(), filter, this.getWindow());
        if (selected == null) {
            return;
        }
        SQLiteDataImportFile importFile = new SQLiteDataImportFile();
        importFile.setFile(selected);
        importFile.setClient(this.dbClient);
        this.importFileTableView.addItem(importFile);
    }

    /**
     * 删除文件
     */
    @FXML
    private void deleteFile() {
        this.importFileTableView.removeSelectedItem();
    }
}
