package cn.oyzh.easyshell.tabs.postgresql.query;

import cn.oyzh.common.util.TextUtil;
import cn.oyzh.easyshell.postgresql.column.PostgresqlColumn;
import cn.oyzh.easyshell.data.postgresql.dto.ShellPostgresqlDataExportTable;
import cn.oyzh.easyshell.query.postgresql.PostgresqlExecuteResult;
import cn.oyzh.easyshell.postgresql.record.PostgresqlDeleteRecordParam;
import cn.oyzh.easyshell.postgresql.record.PostgresqlInsertRecordParam;
import cn.oyzh.easyshell.postgresql.record.PostgresqlRecord;
import cn.oyzh.easyshell.postgresql.record.PostgresqlRecordPrimaryKey;
import cn.oyzh.easyshell.postgresql.record.PostgresqlSelectRecordParam;
import cn.oyzh.easyshell.postgresql.record.PostgresqlUpdateRecordParam;
import cn.oyzh.easyshell.domain.ShellQuery;
import cn.oyzh.easyshell.fx.postgresql.record.ShellPostgresqlRecordColumn;
import cn.oyzh.easyshell.fx.postgresql.record.ShellPostgresqlRecordTableView;
import cn.oyzh.easyshell.trees.postgresql.schema.ShellPostgresqlSchemaTreeItem;
import cn.oyzh.easyshell.util.postgresql.ShellPostgresqlViewFactory;
import cn.oyzh.fx.db.DBObjectList;
import cn.oyzh.fx.db.DBRecordData;
import cn.oyzh.fx.db.listener.DBStatusListener;
import cn.oyzh.fx.db.listener.DBStatusListenerManager;
import cn.oyzh.fx.db.ui.DBStatusColumn;
import cn.oyzh.fx.db.util.DBUtil;
import cn.oyzh.fx.gui.tabs.RichTabController;
import cn.oyzh.fx.plus.controls.box.FXVBox;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.fx.plus.controls.tab.FXTabPane;
import cn.oyzh.fx.plus.controls.table.FXTableColumn;
import cn.oyzh.fx.plus.controls.text.FXText;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.node.NodeGroupUtil;
import cn.oyzh.fx.plus.node.NodeUtil;
import cn.oyzh.fx.plus.window.StageManager;
import cn.oyzh.easyshell.util.ShellI18nHelper;
import cn.oyzh.i18n.I18nHelper;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.event.Event;
import javafx.fxml.FXML;

import java.util.ArrayList;
import java.util.List;

/**
 * PostgreSQL查询结果标签页控制器，负责查询结果的展示与记录的增删改操作
 *
 * @author oyzh
 * @since 2025-11-06
 */
public class ShellPostgresqlQuerySelectTabController extends RichTabController {

    /**
     * 根节点
     */
    @FXML
    private FXVBox root;

    /**
     * sql组件
     */
    @FXML
    private FXText sql;

    /**
     * 耗时组件
     */
    @FXML
    private FXText used;

    /**
     * 计数组件
     */
    @FXML
    private FXText count;

    /**
     * 数据表单组件
     */
    @FXML
    private ShellPostgresqlRecordTableView recordTable;

    /**
     * 数据库树节点
     */
    private ShellPostgresqlSchemaTreeItem dbItem;

    /**
     * 执行结果
     */
    private PostgresqlExecuteResult result;

    /**
     * 新增
     */
    @FXML
    private SVGGlyph add;

    /**
     * 删除
     */
    @FXML
    private SVGGlyph delete;

    /**
     * 应用
     */
    @FXML
    private SVGGlyph apply;

    /**
     * 抛弃
     */
    @FXML
    private SVGGlyph discard;

    /**
     * 记录变更监听器
     */
    private DBStatusListener changeListener;

    /**
     * 字段列表
     */
    private List<PostgresqlColumn> columns;

    /**
     * 执行初始化
     *
     * @param result 执行结果
     * @param dbItem db树表节点
     */
    public void init(PostgresqlExecuteResult result, ShellPostgresqlSchemaTreeItem dbItem) {
        this.result = result;
        this.dbItem = dbItem;
        if (result.isUpdatable()) {
            if (this.changeListener == null) {
                this.changeListener = new DBStatusListener(this.result.schema() + ":" + this.result.tableName()) {
                    @Override
                    public void changed(ObservableValue<?> observable, Object oldValue, Object newValue) {
                        apply.enable();
                    }
                };
            }
            // 部分按钮显示处理
            if (result.isFullColumn()) {
                this.add.display();
            }
            this.apply.display();
            this.delete.display();
            this.discard.display();
        }
        this.initDataList();
    }

    /**
     * 初始化数据列表
     */
    private void initDataList() {
        try {
            // 初始化字段
            this.initColumns(this.result.columnList());
            // 初始化数据
            this.initRecords(this.result.getRecords());
            // 初始化sql信息
            this.sql.text(TextUtil.toSingleLine(this.result.getContent()));
            this.used.text(ShellI18nHelper.queryTime(this.result.getUsedMs()));
            // 初始化计数
            this.initCount(this.result.getCount());
        } catch (Exception ex) {
            MessageBox.exception(ex, this.getWindow());
        }
    }

    /**
     * 初始化计数
     *
     * @param count 计数
     */
    private void initCount(int count) {
        this.count.text(ShellI18nHelper.queryTotal(count));
    }

    /**
     * 初始化列
     *
     * @param columns 列数据
     */
    private void initColumns(List<PostgresqlColumn> columns) {
        // 设置字段列表
        this.columns = columns;
        // 数据列集合
        List<FXTableColumn<PostgresqlRecord, Object>> columnList = new ArrayList<>();
        DBStatusColumn<PostgresqlRecord> statusColumn = new DBStatusColumn<>();
        columnList.add(statusColumn);
        for (PostgresqlColumn column : columns) {
            ShellPostgresqlRecordColumn tableColumn = new ShellPostgresqlRecordColumn(column);
            tableColumn.setPrefWidth(DBUtil.suitableColumnWidth(column));
            columnList.add(tableColumn);
        }
        this.recordTable.getColumns().setAll(columnList);
    }

    /**
     * 初始化记录
     *
     * @param records 数据
     */
    private void initRecords(List<PostgresqlRecord> records) {
        this.recordTable.setItem(records);
    }

    /**
     * 添加记录
     */
    @FXML
    private void addRecord() {
        PostgresqlRecord record = new PostgresqlRecord(this.columns);
        record.setCreated(true);
        for (PostgresqlColumn column : this.columns) {
            Object val = null;
            if (column.supportDefaultValue()) {
                val = column.getDefaultValue();
            }
            record.putValue(column, val);
        }
        this.recordTable.addItem(record);
        this.recordTable.selectLast();
        // 初始化计数
        this.initCount(this.recordTable.getItemSize());
    }

    /**
     * 插入记录
     *
     * @param record 记录
     */
    private void insertRecord(PostgresqlRecord record) {
        DBRecordData recordData = record.getRecordData();
        PostgresqlRecordPrimaryKey primaryKey = this.initPrimaryKey(record);
        PostgresqlInsertRecordParam param = new PostgresqlInsertRecordParam();
        param.setRecord(recordData);
        param.setPrimaryKey(primaryKey);
        param.setSchema(this.result.schema());
        param.setTableName(this.result.tableName());
        this.dbItem.client().insertRecord(param);
        if (primaryKey != null) {
            PostgresqlSelectRecordParam selectRecordParam = new PostgresqlSelectRecordParam();
            selectRecordParam.setPrimaryKey(primaryKey);
            selectRecordParam.setSchema(this.result.schema());
            selectRecordParam.setTableName(this.result.tableName());
            // 处理回显
            record.copy(this.dbItem.client().selectRecord(selectRecordParam));
        }
    }

    /**
     * 更改记录
     *
     * @param record 记录
     */
    private void updateRecord(PostgresqlRecord record) {
        // 获取主键
        PostgresqlRecordPrimaryKey primaryKey = this.initPrimaryKey(record);
        PostgresqlUpdateRecordParam param = new PostgresqlUpdateRecordParam();
        param.setSchema(this.result.schema());
        param.setTableName(this.result.tableName());
        // 主键存在，则根据主键更新
        if (primaryKey != null) {
            // 记录数据
            DBRecordData recordData = record.getChangedRecordData();
            // 如果主键未变更，则移除主键数据
            if (!record.isColumnChanged(primaryKey.getColumnName())) {
                recordData.remove(primaryKey.getColumnName());
            }
            if (recordData.isEmpty()) {
                return;
            }
            param.setUpdateRecord(recordData);
            param.setPrimaryKey(primaryKey);
            // 更新行
            this.dbItem.client().updateRecord(param);
            PostgresqlSelectRecordParam selectRecordParam = new PostgresqlSelectRecordParam();
            selectRecordParam.setPrimaryKey(primaryKey);
            selectRecordParam.setSchema(this.result.schema());
            selectRecordParam.setTableName(this.result.tableName());
            // 处理回显
            record.copy(this.dbItem.selectRecord(selectRecordParam));
        } else {// 主键不存在，则根据所有字段更新
            // 变更数据
            DBRecordData changedRecordData = record.getChangedRecordData();
//            // 原始数据
//            DBRecordData originalRecordData = record.getOriginalRecordData();
//            param.setUpdateRecord(originalRecordData);
            param.setUpdateRecord(changedRecordData);
            // 更新行
            this.dbItem.client().updateRecord(param);
        }
    }

    /**
     * 初始化主键
     *
     * @param record 记录
     * @return 主键
     */
    private PostgresqlRecordPrimaryKey initPrimaryKey(PostgresqlRecord record) {
        PostgresqlColumn primaryKeyColumn = this.result.getPrimaryKey();
        // 主键
        if (primaryKeyColumn != null) {
            PostgresqlRecordPrimaryKey primaryKey = new PostgresqlRecordPrimaryKey();
            primaryKey.init(primaryKeyColumn, record);
            return primaryKey;
        }
        // 序列
        for (PostgresqlColumn column : record.getColumns()) {
            if(column.isDefaultValueSequence()){
                PostgresqlRecordPrimaryKey primaryKey = new PostgresqlRecordPrimaryKey();
                primaryKey.init(column, record);
                return primaryKey;
            }
        }
        return null;
    }

    /**
     * 应用变更
     */
    @FXML
    private void apply() {
        if (this.apply.isEnable()) {
            try {
                List<PostgresqlRecord> records = this.recordTable.getItems();
                for (PostgresqlRecord record : records) {
                    if (DBObjectList.isCreated(record)) {
                        this.insertRecord(record);
                        record.clearStatus();
                    } else if (DBObjectList.isChanged(record)) {
                        this.updateRecord(record);
                        record.clearStatus();
                    }
                }
                this.apply.disable();
            } catch (Exception ex) {
                MessageBox.exception(ex, this.getWindow());
            }
        }
    }

    /**
     * 丢弃变更
     */
    @FXML
    private void discard() {
        try {
            PostgresqlRecord discardRecord = null;
            for (PostgresqlRecord record : this.recordTable.getItems()) {
                if (record.isCreated()) {
                    discardRecord = record;
                } else if (record.isChanged()) {
                    record.discard();
                }
            }
            this.recordTable.removeItem(discardRecord);
            this.apply.disable();
            // 初始化计数
            this.initCount(this.recordTable.getItemSize());
        } catch (Exception ex) {
            MessageBox.exception(ex, this.getWindow());
        }
    }

    /**
     * 刷新记录
     */
    @FXML
    private void reload() {
        try {
            // 检查是否有未保存的数据
            if (this.apply.isEnable() && !MessageBox.confirm(I18nHelper.unsavedAndContinue(), this.getWindow())) {
                return;
            }
            // 执行查询
            this.result = this.dbItem.executeSingleSql(this.result.getContent());
            // 初始化数据
            this.initDataList();
            // 禁用组件
            this.apply.disable();
        } catch (Exception ex) {
            MessageBox.exception(ex, this.getWindow());
        }
    }

    /**
     * 导出记录
     */
    @FXML
    private void exportRecord() {
        try {
            FXTabPane tabPane = (FXTabPane) this.getTabPane();
            ShellQuery query = tabPane.getProp("query");
            if (query.getName() == null) {
                query.setName(I18nHelper.unnamedQuery());
            }
            ShellPostgresqlDataExportTable exportTable = new ShellPostgresqlDataExportTable();
            exportTable.setSelected(true);
            exportTable.setName(query.getName());
            exportTable.columns(this.result.getColumns());
            exportTable.setRecords(this.result.getRecords());
            ShellPostgresqlViewFactory.exportData(this.dbItem.client(), this.result.schema(), null, 1, exportTable);
        } catch (Exception ex) {
            MessageBox.exception(ex, this.getWindow());
        }
    }

    /**
     * 删除记录
     */
    @FXML
    private void deleteRecord() {
        List<PostgresqlRecord> records = new ArrayList<>(this.recordTable.getSelectedItems());
        if (!MessageBox.confirm(ShellI18nHelper.confirm("deleteRecord"), this.getWindow())) {
            return;
        }
        StageManager.showMask(this.getWindow(), () -> this.deleteRecords(records));
    }

    /**
     * 删除记录
     *
     * @param records 记录
     */
    private void deleteRecords(List<PostgresqlRecord> records) {
        try {
            boolean success = false;
            for (PostgresqlRecord record : records) {
                success = this.deleteRecord(record);
                if (!success) {
                    break;
                }
            }
            // 操作成功
            if (success) {
                this.recordTable.removeItem(records);
                // 初始化计数
                this.initCount(this.recordTable.getItemSize());
            } else {// 操作失败
                MessageBox.warnToast(I18nHelper.operationFail(), this.getWindow());
            }
        } catch (Exception ex) {
            MessageBox.exception(ex, this.getWindow());
        }
    }

    /**
     * 删除表记录
     *
     * @param record 表记录
     * @return 结果
     */
    private boolean deleteRecord(PostgresqlRecord record) {
        // 如果是新增的数据，直接删除
        boolean success;
        if (record.isCreated()) {
            success = true;
        } else {
            // 获取主键
            PostgresqlRecordPrimaryKey primaryKey = this.initPrimaryKey(record);
            PostgresqlDeleteRecordParam param = new PostgresqlDeleteRecordParam();
            param.setSchema(this.result.schema());
            param.setTableName(this.result.tableName());
            param.setPrimaryKey(primaryKey);
            param.setRecord(record.getOriginalRecordData());
            success = this.dbItem.deleteRecord(param) == 1;
        }
        if (success) {
            record.destroy();
        }
        return success;
    }

    @Override
    public void onTabClosed(Event event) {
        super.onTabClosed(event);
        //        this.recordTable.destroy();
        DBStatusListenerManager.removeListener(this.changeListener);
    }

    @Override
    protected void bindListeners() {
        super.bindListeners();
        this.recordTable.selectedItemChanged((observable, oldValue, newValue) -> {
            if (newValue != null) {
                newValue.setEditable(true);
            }
            this.recordTable.refresh();
        });
        this.discard.disableProperty().bind(this.apply.disableProperty());
        this.apply.disabledProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                NodeGroupUtil.enable(this.root, "action2");
            } else {
                NodeGroupUtil.disable(this.root, "action2");
            }
        });
        this.recordTable.getItems().addListener((ListChangeListener<PostgresqlRecord>) c -> {
            if (c.next() && c.wasAdded()) {
                List<? extends PostgresqlRecord> rows = c.getAddedSubList();
                for (PostgresqlRecord row : rows) {
                    if (DBObjectList.isCreated(row)) {
                        this.apply.enable();
                        break;
                    }
                }
            }
        });
        this.recordTable.setCtrlSAction(this::apply);
        NodeUtil.nodeOnCtrlS(this.root, this::apply);
    }
}
