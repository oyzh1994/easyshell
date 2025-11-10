package cn.oyzh.easyshell.controller.mysql.data;

import cn.oyzh.common.date.DateUtil;
import cn.oyzh.common.system.SystemUtil;
import cn.oyzh.common.thread.ThreadUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.fx.mysql.data.DataDateTextFiled;
import cn.oyzh.easyshell.fx.mysql.data.DataExportColumnListView;
import cn.oyzh.easyshell.fx.mysql.data.DataExportTable;
import cn.oyzh.easyshell.fx.mysql.data.DataExportTableComboBox;
import cn.oyzh.easyshell.fx.mysql.data.DataExportTableTableView;
import cn.oyzh.easyshell.fx.mysql.data.DataFieldSeparatorComboBox;
import cn.oyzh.easyshell.fx.mysql.data.DataRecordSeparatorComboBox;
import cn.oyzh.easyshell.fx.mysql.data.DataTxtIdentifierComboBox;
import cn.oyzh.easyshell.db.handler.DBDataExportHandler;
import cn.oyzh.easyshell.mysql.ShellMysqlClient;
import cn.oyzh.easyshell.mysql.column.MysqlSelectColumnParam;
import cn.oyzh.easyshell.mysql.table.MysqlTable;
import cn.oyzh.fx.gui.text.area.MsgTextArea;
import cn.oyzh.fx.plus.FXConst;
import cn.oyzh.fx.plus.chooser.FXChooser;
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
import cn.oyzh.fx.plus.window.StageAdapter;
import cn.oyzh.fx.plus.window.StageAttribute;
import cn.oyzh.i18n.I18nHelper;
import javafx.fxml.FXML;
import javafx.scene.control.RadioButton;
import javafx.stage.Modality;
import javafx.stage.WindowEvent;

import java.util.Date;
import java.util.List;


/**
 * db数据导出业务
 *
 * @author oyzh
 * @since 2024/08/26
 */
@StageAttribute(
        modality = Modality.WINDOW_MODAL,
        value = FXConst.FXML_PATH + "mysql/data/shellMysqlDataExport.fxml"
)
public class ShellMysqlDataExportController extends StageController {

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
     * 导出表下拉框
     */
    @FXML
    private DataExportTableComboBox tableCombobox;

    /**
     * 导出表字段列表
     */
    @FXML
    private DataExportColumnListView tableColumns;

    /**
     * 导出表组件
     */
    @FXML
    private DataExportTableTableView exportTableView;

    // /**
    //  * 导出表已选择列
    //  */
    // @FXML
    // private FXTableColumn<DataExportTable, String> exportTableSelected;
    //
    // /**
    //  * 导出表名称列
    //  */
    // @FXML
    // private FXTableColumn<DataExportTable, String> exportTableName;
    //
    // /**
    //  * 导出表路径列
    //  */
    // @FXML
    // private FXTableColumn<DataExportTable, String> exportTableFilePath;

    /**
     * 文件类型
     */
    @FXML
    private FXToggleGroup fileType;

    /**
     * db客户端
     */
    private ShellMysqlClient dbClient;

    /**
     * 日期预览
     */
    @FXML
    private FXLabel datePreview;

    /**
     * 日期格式
     */
    @FXML
    private DataDateTextFiled dateFormat;

    /**
     * 记录分隔符
     */
    @FXML
    private DataRecordSeparatorComboBox recordSeparator;

    /**
     * 字段分割符
     */
    @FXML
    private DataFieldSeparatorComboBox fieldSeparator;

    /**
     * 文本识别符
     */
    @FXML
    private DataTxtIdentifierComboBox txtIdentifier;

    /**
     * 包含列标题
     */
    @FXML
    private FXCheckBox includeFields;

    /**
     * 字段作为属性
     */
    @FXML
    private FXCheckBox fieldToAttr;

    /**
     * 早期版本
     */
    @FXML
    private FXCheckBox earlyVersion;

    /**
     * 结束导出按钮
     */
    @FXML
    private FXButton stopExportBtn;

    /**
     * 导出状态
     */
    @FXML
    private FXLabel exportStatus;

    /**
     * 导出消息
     */
    @FXML
    private MsgTextArea exportMsg;

    /**
     * 导出操作任务
     */
    private Thread execTask;

    /**
     * 计数器
     */
    private final Counter counter = new Counter();

    /**
     * 导出处理器
     */
    private DBDataExportHandler exportHandler;

    /**
     * 数据库
     */
    private String dbName;

    /**
     * 表
     */
    private String tableName;

    /**
     * 执行导出
     */
    @FXML
    private void doExport() {
        // 重置参数
        this.counter.reset();
        this.exportMsg.clear();
        // 开始处理
        this.exportMsg.clear();
        // 生成导出处理器
        if (this.exportHandler == null) {
            this.exportHandler = new DBDataExportHandler(this.dbClient, this.dbName);
            this.exportHandler.setMessageHandler(str -> this.exportMsg.appendLine(str))
                    .setProcessedHandler(count -> {
                        if (count > 0) {
                            this.counter.incrSuccess(count);
                        } else {
                            this.counter.incrFail(Math.abs(count));
                        }
                        this.updateStatus(I18nHelper.exportInProgress());
                    });
        } else {
            this.exportHandler.interrupt(false);
        }
        // 文件类型
        this.exportHandler.setFileType(this.fileType.selectedUserData());
        // 表
        this.exportHandler.setTables(this.exportTableView.getSelectedTables());
        // 根据不同类型设置不同分页策略
        if (!this.exportHandler.isExcelType()) {
            this.exportHandler.setQueryLimit(10_000);
        }
        // 日期格式
        this.exportHandler.dateFormat(this.dateFormat.getTextTrim());
        // 字段作为属性
        this.exportHandler.fieldToAttr(this.fieldToAttr.isSelected());
        // 早期版本
        this.exportHandler.earlyVersion(this.earlyVersion.isSelected());
        // 字段分隔符
        this.exportHandler.fieldSeparator(this.fieldSeparator.value());
        // 记录分隔符
        this.exportHandler.recordSeparator(this.recordSeparator.value());
        // 包含列标题
        this.exportHandler.includeFields(this.includeFields.isSelected());
        // 文本识别符
        this.exportHandler.txtIdentifier(this.txtIdentifier.getSelectedItem());
        NodeGroupUtil.disable(this.stage, "exec");
        this.stage.appendTitle("===" + I18nHelper.exportInProgress() + "===");
        // 执行导出
        this.execTask = ThreadUtil.start(() -> {
            try {
                this.stopExportBtn.enable();
                // 更新状态
                this.updateStatus(I18nHelper.exportStarting());
                // 执行导出
                this.exportHandler.doExport();
                // 更新状态
                this.updateStatus(I18nHelper.exportFinished());
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
                this.stopExportBtn.disable();
                this.stage.restoreTitle();
                SystemUtil.gcLater();
            }
        });
    }

    /**
     * 结束导出
     */
    @FXML
    private void stopExport() {
        ThreadUtil.interrupt(this.execTask);
        this.execTask = null;
        if (this.exportHandler != null) {
            this.exportHandler.interrupt();
        }
    }

    @Override
    protected void bindListeners() {
        super.bindListeners();
        // this.exportTableName.setCellValueFactory(new PropertyValueFactory<>("name"));
        // this.exportTableSelected.setCellValueFactory(new PropertyValueFactory<>("selectedControl"));
        // this.exportTableFilePath.setCellValueFactory(new PropertyValueFactory<>("filePathControl"));
        this.tableCombobox.selectedItemChanged((observable, oldValue, newValue) -> {
            if (newValue != null) {
                this.tableColumns.init(newValue.getColumns());
            } else {
                this.tableColumns.clearItems();
            }
        });
        this.dateFormat.textProperty().addListener((observable, oldValue, newValue) -> this.flushDatePreview());
    }

    private void flushDatePreview() {
        try {
            String format = this.dateFormat.getTextTrim();
            this.datePreview.setText(I18nHelper.currentTime() + " " + DateUtil.format(new Date(), format));
        } catch (Exception ex) {
            this.datePreview.setText(I18nHelper.invalidFormat());
        }
    }

    @Override
    public void onWindowShown(WindowEvent event) {
        super.onWindowShown(event);
        this.dbName = this.getProp("dbName");
        this.dbClient = this.getProp("dbClient");
        this.tableName = this.getProp("tableName");
        this.stage.hideOnEscape();
    }

    @Override
    public void onWindowHidden(WindowEvent event) {
        this.stopExport();
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
        FXUtil.runLater(() -> this.exportStatus.setText(this.counter.unknownFormat()));
    }

    @Override
    public String getViewTitle() {
        return I18nHelper.exportTitle();
    }

    @Override
    public void onStageInitialize(StageAdapter stage) {
        super.onStageInitialize(stage);
        this.step1.managedBindVisible();
        this.step2.managedBindVisible();
        this.step3.managedBindVisible();
        this.step4.managedBindVisible();
        this.step5.managedBindVisible();
    }

    @FXML
    private void showStep1() {
        this.step1.display();
        this.step2.disappear();
    }

    @FXML
    private void showStep2() {
        RadioButton button = this.fileType.selectedToggle();
        if (button == null) {
            MessageBox.warn(I18nHelper.pleaseSelectType());
            return;
        }
        if (this.exportTableView.isItemEmpty()) {
            List<MysqlTable> tables = this.dbClient.selectTables(this.dbName);
            for (MysqlTable table : tables) {
                DataExportTable exportTable = new DataExportTable();
                exportTable.setName(table.getName());
                exportTable.setSelected(StringUtil.equals(table.getName(), this.tableName));
                this.exportTableView.addItem(exportTable);
            }
        }
        for (DataExportTable exportTable : this.exportTableView.getItems()) {
            exportTable.setExtension(FXChooser.extensionFilter(button.getUserData().toString()));
        }
        this.step1.disappear();
        this.step3.disappear();
        this.step2.display();
    }

    @FXML
    private void showStep3() {
        if (!this.exportTableView.hasSelectedTable()) {
            MessageBox.warn(I18nHelper.pleaseSelectTable());
            return;
        }
        this.tableCombobox.clearItems();
        for (DataExportTable o : this.exportTableView.getSelectedTables()) {
            if (!o.hasColumns()) {
                o.columns(this.dbClient.selectColumns(new MysqlSelectColumnParam(this.dbName, o.getName())));
            }
            this.tableCombobox.addItem(o);
        }
        this.tableCombobox.selectFirst();
        this.step2.disappear();
        this.step4.disappear();
        this.step3.display();
    }

    @FXML
    private void showStep4() {
        this.step3.disappear();
        // 文件类型
        String type = this.fileType.selectedUserData();
        // 显示对应组件
        if ("sql".equalsIgnoreCase(type)) {
            NodeGroupUtil.display(this.getStage(), "includeFields");
            NodeGroupUtil.disappear(this.getStage(), "txtIdentifier");
            NodeGroupUtil.disappear(this.getStage(), "fieldSeparator");
            NodeGroupUtil.disappear(this.getStage(), "recordSeparator");
            NodeGroupUtil.disappear(this.getStage(), "dateFormat");
            NodeGroupUtil.disappear(this.getStage(), "fieldToAttr");
            NodeGroupUtil.disappear(this.getStage(), "earlyVersion");
        } else if ("txt".equalsIgnoreCase(type)) {
            NodeGroupUtil.display(this.getStage(), "txtIdentifier");
            NodeGroupUtil.display(this.getStage(), "fieldSeparator");
            NodeGroupUtil.display(this.getStage(), "recordSeparator");
            NodeGroupUtil.display(this.getStage(), "dateFormat");
            NodeGroupUtil.disappear(this.getStage(), "includeFields");
            NodeGroupUtil.disappear(this.getStage(), "fieldToAttr");
            NodeGroupUtil.disappear(this.getStage(), "earlyVersion");
        } else if ("json".equalsIgnoreCase(type)) {
            NodeGroupUtil.display(this.getStage(), "dateFormat");
            NodeGroupUtil.display(this.getStage(), "earlyVersion");
            NodeGroupUtil.disappear(this.getStage(), "txtIdentifier");
            NodeGroupUtil.disappear(this.getStage(), "fieldSeparator");
            NodeGroupUtil.disappear(this.getStage(), "recordSeparator");
            NodeGroupUtil.disappear(this.getStage(), "includeFields");
            NodeGroupUtil.disappear(this.getStage(), "fieldToAttr");
        } else if (StringUtil.equalsAnyIgnoreCase(type, "xls", "xlsx")) {
            NodeGroupUtil.disappear(this.getStage(), "dateFormat");
            NodeGroupUtil.disappear(this.getStage(), "txtIdentifier");
            NodeGroupUtil.disappear(this.getStage(), "fieldSeparator");
            NodeGroupUtil.disappear(this.getStage(), "recordSeparator");
            NodeGroupUtil.disappear(this.getStage(), "includeFields");
            NodeGroupUtil.disappear(this.getStage(), "fieldToAttr");
            NodeGroupUtil.disappear(this.getStage(), "earlyVersion");
        } else if (StringUtil.equalsIgnoreCase(type, "csv")) {
            NodeGroupUtil.display(this.getStage(), "txtIdentifier");
            NodeGroupUtil.display(this.getStage(), "recordSeparator");
            NodeGroupUtil.display(this.getStage(), "dateFormat");
            NodeGroupUtil.disappear(this.getStage(), "fieldSeparator");
            NodeGroupUtil.disappear(this.getStage(), "includeFields");
            NodeGroupUtil.disappear(this.getStage(), "fieldToAttr");
            NodeGroupUtil.disappear(this.getStage(), "earlyVersion");
        } else if (StringUtil.equalsIgnoreCase(type, "html")) {
            NodeGroupUtil.display(this.getStage(), "dateFormat");
            NodeGroupUtil.disappear(this.getStage(), "txtIdentifier");
            NodeGroupUtil.disappear(this.getStage(), "recordSeparator");
            NodeGroupUtil.disappear(this.getStage(), "fieldSeparator");
            NodeGroupUtil.disappear(this.getStage(), "includeFields");
            NodeGroupUtil.disappear(this.getStage(), "fieldToAttr");
            NodeGroupUtil.disappear(this.getStage(), "earlyVersion");
        } else if (StringUtil.equalsIgnoreCase(type, "xml")) {
            NodeGroupUtil.display(this.getStage(), "dateFormat");
            NodeGroupUtil.display(this.getStage(), "fieldToAttr");
            NodeGroupUtil.disappear(this.getStage(), "txtIdentifier");
            NodeGroupUtil.disappear(this.getStage(), "recordSeparator");
            NodeGroupUtil.disappear(this.getStage(), "fieldSeparator");
            NodeGroupUtil.disappear(this.getStage(), "includeFields");
            NodeGroupUtil.disappear(this.getStage(), "earlyVersion");
        }
        this.step5.disappear();
        this.flushDatePreview();
        this.step4.display();
    }

    @FXML
    private void showStep5() {
        this.step4.disappear();
        this.step5.display();
    }

    @FXML
    private void selectAllTable() {
        for (DataExportTable item : this.exportTableView.getItems()) {
            item.setSelected(true);
        }
    }

    @FXML
    private void unselectAllTable() {
        for (DataExportTable item : this.exportTableView.getItems()) {
            item.setSelected(false);
        }
    }

    @FXML
    private void selectAllFiled() {
        for (FXCheckBox item : this.tableColumns.getItems()) {
            item.setSelected(true);
        }
    }

    @FXML
    private void unselectAllField() {
        for (FXCheckBox item : this.tableColumns.getItems()) {
            item.setSelected(false);
        }
    }
}
