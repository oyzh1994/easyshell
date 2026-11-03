package cn.oyzh.easyshell.controller.sqlite.data;

import cn.oyzh.common.date.DateUtil;
import cn.oyzh.common.system.SystemUtil;
import cn.oyzh.common.thread.ThreadUtil;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.data.sqlite.dto.SQLiteDataExportTable;
import cn.oyzh.easyshell.data.sqlite.handler.SQLiteDataExportHandler;
import cn.oyzh.easyshell.data.sqlite.ui.SQLiteDataExportColumnListView;
import cn.oyzh.easyshell.data.sqlite.ui.SQLiteDataExportTableTableView;
import cn.oyzh.easyshell.sqlite.table.SQLiteTable;
import cn.oyzh.easyshell.sqlite.view.SQLiteView;
import cn.oyzh.easyshell.sqlite.ShellSqliteClient;
import cn.oyzh.fx.db.data.ui.DBDataDateTextFiled;
import cn.oyzh.fx.db.data.ui.DBDataFieldSeparatorComboBox;
import cn.oyzh.fx.db.data.ui.DBDataRecordSeparatorComboBox;
import cn.oyzh.fx.db.data.ui.DBDataTxtIdentifierComboBox;
import cn.oyzh.fx.db.ui.DBNameComboBox;
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
import cn.oyzh.fx.plus.window.FXStageStyle;
import cn.oyzh.fx.plus.window.StageAttribute;
import cn.oyzh.fx.plus.window.StageManager;
import cn.oyzh.i18n.I18nHelper;
import javafx.fxml.FXML;
import javafx.scene.control.RadioButton;
import javafx.stage.WindowEvent;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * SQLite 数据导出业务。
 *
 * @author oyzh
 * @since 2026-11-02
 */
@StageAttribute(
        multipliable = true,
        stageStyle = FXStageStyle.EXTENDED,
        value = FXConst.FXML_PATH + "sqlite/data/shellSqliteDataExport.fxml"
)
public class ShellSqliteDataExportController extends StageController {

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
     * 文件类型
     */
    @FXML
    private FXToggleGroup fileType;

    /**
     * 导出表下拉框
     */
    @FXML
    private DBNameComboBox tableCombobox;

    /**
     * 导出表字段列表
     */
    @FXML
    private SQLiteDataExportColumnListView tableColumns;

    /**
     * 导出表组件
     */
    @FXML
    private SQLiteDataExportTableTableView exportTableView;

    /**
     * 日期格式
     */
    @FXML
    private DBDataDateTextFiled dateFormat;

    /**
     * 日期预览
     */
    @FXML
    private FXLabel datePreview;

    /**
     * 记录分隔符
     */
    @FXML
    private DBDataRecordSeparatorComboBox recordSeparator;

    /**
     * 字段分隔符
     */
    @FXML
    private DBDataFieldSeparatorComboBox fieldSeparator;

    /**
     * 文本识别符
     */
    @FXML
    private DBDataTxtIdentifierComboBox txtIdentifier;

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
     * 遇到错误时继续
     */
    @FXML
    private FXCheckBox continueWithError;

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
    private SQLiteDataExportHandler exportHandler;

    /**
     * db客户端
     */
    private ShellSqliteClient dbClient;

    /**
     * 表
     */
    private String tableName;

    /**
     * 0: 正常导出
     * 1: 查询导出
     */
    private int exportMode = 0;

    /**
     * 导出表
     */
    private SQLiteDataExportTable exportTable;

    /**
     * 执行导出
     */
    @FXML
    private void doExport() {
        // 重置参数
        this.counter.reset();
        // 开始处理
        this.exportMsg.clear();
        // 生成导出处理器
        if (this.exportHandler == null) {
            this.exportHandler = new SQLiteDataExportHandler(this.dbClient, "main");
            this.exportHandler.setMessageHandler(this.exportMsg::appendLine)
                    .setProcessedHandler(count -> {
                        this.counter.incr(count);
                        this.updateStatus(I18nHelper.exportInProgress());
                    });
        } else {
            this.exportHandler.interrupt(false);
        }
        // 文件类型
        this.exportHandler.setFileType(this.selectedFileType());
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
        this.exportHandler.includeTitle(this.includeFields.isSelected());
        // 文本识别符
        this.exportHandler.txtIdentifier(this.txtIdentifier.getSelectedItem());
        // 错误时继续
        this.exportHandler.setContinueWithErrors(this.continueWithError.isSelected());
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
            } catch (Exception ex) {
                if (ex instanceof InterruptedException) {
                    this.updateStatus(I18nHelper.operationCancel());
                    MessageBox.okToast(I18nHelper.operationCancel(), this.getWindow());
                } else {
                    this.updateStatus(I18nHelper.operationFail());
                    MessageBox.exception(ex, this.getWindow());
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
        this.tableCombobox.selectedItemChanged((observable, oldValue, newValue) -> {
            if (newValue instanceof SQLiteDataExportTable table) {
                this.tableColumns.init(table.getColumns());
            } else {
                this.tableColumns.clearItems();
            }
        });
        this.dateFormat.textProperty().addListener((observable, oldValue, newValue) -> this.flushDatePreview());
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
    public void onWindowShown(WindowEvent event) {
        this.dbClient = this.getProp("dbClient");
        this.tableName = this.getProp("tableName");
        if (this.hasProp("exportMode")) {
            this.exportMode = this.getProp("exportMode");
        }
        if (this.hasProp("exportTable")) {
            this.exportTable = this.getProp("exportTable");
        }
        if (this.exportMode == 1 && this.exportTable != null) {
            this.initTables();
            this.showStep4();
        }
        this.stage.hideOnEscape();
        super.onWindowShown(event);
    }

    @Override
    public void onWindowHidden(WindowEvent event) {
        this.stopExport();
        super.onWindowHidden(event);
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
        this.exportStatus.text(this.counter.unknownFormat());
    }

    @Override
    public String getViewTitle() {
        return I18nHelper.exportTitle();
    }

    /**
     * 显示第一步
     */
    @FXML
    private void showStep1() {
        this.step1.display();
        this.step2.disappear();
    }

    /**
     * 初始化表列表
     */
    private void initTables() {
        this.exportTableView.clearItems();
        this.tableCombobox.clearItems();
        List<SQLiteDataExportTable> exportTables = new ArrayList<>();
        // 查询导出
        if (this.exportTable != null) {
            this.exportTable.setSelected(true);
            if (this.exportTable.getExtension() == null) {
                this.exportTable.setExtension(FXChooser.extensionFilter(this.selectedFileType()));
            }
            exportTables.add(this.exportTable);
            SQLiteTable table = this.dbClient.selectTable(this.exportTable.getName());
            if (table == null) {
                table = this.exportTable(this.exportTable.getName());
            }
            this.tableCombobox.addItem(table);
        } else {// 正常导出
            List<SQLiteTable> tables = new ArrayList<>();
            if (StringUtil.isBlank(this.tableName)) {
                tables.addAll(this.dbClient.selectTables());
                for (SQLiteView view : this.dbClient.selectViews()) {
                    tables.add(this.exportTable(view));
                }
            } else {
                SQLiteTable table = this.dbClient.selectTable(this.tableName);
                if (table != null) {
                    tables.add(table);
                } else {
                    SQLiteView view = this.dbClient.selectView(this.tableName);
                    if (view != null) {
                        tables.add(this.exportTable(view));
                    }
                }
            }
            AtomicReference<SQLiteDataExportTable> ref = new AtomicReference<>();
            for (SQLiteTable table : tables) {
                if (table == null) {
                    continue;
                }
                SQLiteDataExportTable exportTable = new SQLiteDataExportTable();
                exportTable.setName(table.getName());
                exportTable.columns(this.dbClient.selectColumns(table.getName()));
                exportTable.setSelected(StringUtil.isNotBlank(this.tableName));
                exportTable.setExtension(FXChooser.extensionFilter(this.selectedFileType()));
                exportTables.add(exportTable);
                this.tableCombobox.addItem(table);
                if (StringUtil.equals(this.tableName, table.getName())) {
                    ref.set(exportTable);
                }
            }
            if (ref.get() != null) {
                FXUtil.runWait(() -> this.exportTableView.scrollTo(ref.get()));
            }
        }
        this.exportTableView.setItem(exportTables);
        if (!exportTables.isEmpty()) {
            this.tableCombobox.selectFirst();
            if (this.tableCombobox.getSelectedItem() instanceof SQLiteDataExportTable table) {
                this.tableColumns.init(table.getColumns());
            } else {
                this.tableColumns.clearItems();
            }
        }
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
        this.applyFileType();
        StageManager.showMask(this.getWindow(), this::initTables);
        this.step1.disappear();
        this.step3.disappear();
        this.step2.display();
    }

    /**
     * 显示第三步
     */
    @FXML
    private void showStep3() {
        if (!this.exportTableView.hasSelectedTable()) {
            MessageBox.warn(I18nHelper.pleaseSelectTable(), this.getWindow());
            return;
        }
        this.tableCombobox.clearItems();
        for (SQLiteDataExportTable table : this.exportTableView.getSelectedTables()) {
            if (!table.hasColumns()) {
                table.columns(this.dbClient.selectColumns(table.getName()));
            }
            this.tableCombobox.addItem(table);
        }
        this.tableCombobox.selectFirst();
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
        this.exportMsg.clear();
        // 文件类型
        String type = this.selectedFileType();
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
        } else if ("csv".equalsIgnoreCase(type)) {
            NodeGroupUtil.display(this.getStage(), "txtIdentifier");
            NodeGroupUtil.display(this.getStage(), "recordSeparator");
            NodeGroupUtil.display(this.getStage(), "dateFormat");
            NodeGroupUtil.disappear(this.getStage(), "fieldSeparator");
            NodeGroupUtil.disappear(this.getStage(), "includeFields");
            NodeGroupUtil.disappear(this.getStage(), "fieldToAttr");
            NodeGroupUtil.disappear(this.getStage(), "earlyVersion");
        } else if ("html".equalsIgnoreCase(type)) {
            NodeGroupUtil.display(this.getStage(), "dateFormat");
            NodeGroupUtil.disappear(this.getStage(), "txtIdentifier");
            NodeGroupUtil.disappear(this.getStage(), "recordSeparator");
            NodeGroupUtil.disappear(this.getStage(), "fieldSeparator");
            NodeGroupUtil.disappear(this.getStage(), "includeFields");
            NodeGroupUtil.disappear(this.getStage(), "fieldToAttr");
            NodeGroupUtil.disappear(this.getStage(), "earlyVersion");
        } else if ("xml".equalsIgnoreCase(type)) {
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

    /**
     * 显示第五步
     */
    @FXML
    private void showStep5() {
        this.step4.disappear();
        this.step5.display();
    }

    /**
     * 全选表
     */
    @FXML
    private void selectAllTable() {
        for (SQLiteDataExportTable item : this.exportTableView.getItems()) {
            item.setSelected(true);
        }
    }

    /**
     * 取消全选表
     */
    @FXML
    private void unselectAllTable() {
        for (SQLiteDataExportTable item : this.exportTableView.getItems()) {
            item.setSelected(false);
        }
    }

    /**
     * 全选字段
     */
    @FXML
    private void selectAllFiled() {
        for (FXCheckBox item : this.tableColumns.getItems()) {
            item.setSelected(true);
        }
    }

    /**
     * 取消全选字段
     */
    @FXML
    private void unselectAllField() {
        for (FXCheckBox item : this.tableColumns.getItems()) {
            item.setSelected(false);
        }
    }

    /**
     * 当前文件类型
     *
     * @return 文件类型
     */
    private String selectedFileType() {
        return this.fileType.selectedUserData();
    }

    /**
     * 应用文件类型
     */
    private void applyFileType() {
        String type = this.selectedFileType();
        for (SQLiteDataExportTable table : this.exportTableView.getItems()) {
            table.setExtension(FXChooser.extensionFilter(type));
        }
    }

    /**
     * 将视图转换为导出表
     *
     * @param view 视图
     * @return 导出表
     */
    private SQLiteTable exportTable(SQLiteView view) {
        SQLiteTable table = new SQLiteTable();
        table.setName(view.getName());
        table.setType("view");
        table.setDefinition(view.getDefinition());
        return table;
    }

    /**
     * 将名称转换为导出表
     *
     * @param name 名称
     * @return 导出表
     */
    private SQLiteTable exportTable(String name) {
        SQLiteTable table = new SQLiteTable();
        table.setName(name);
        return table;
    }
}
