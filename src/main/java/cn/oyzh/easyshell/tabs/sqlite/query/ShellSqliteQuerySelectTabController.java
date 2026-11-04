package cn.oyzh.easyshell.tabs.sqlite.query;

import cn.oyzh.common.util.TextUtil;
import cn.oyzh.easyshell.data.sqlite.dto.SQLiteDataExportTable;
import cn.oyzh.easyshell.fx.sqlite.record.ShellSqliteRecordColumn;
import cn.oyzh.easyshell.fx.sqlite.record.ShellSqliteRecordTableView;
import cn.oyzh.easyshell.query.sqlite.SQLiteExecuteResult;
import cn.oyzh.easyshell.sqlite.column.SQLiteColumn;
import cn.oyzh.easyshell.sqlite.column.SQLiteColumns;
import cn.oyzh.easyshell.sqlite.record.SQLiteDeleteRecordParam;
import cn.oyzh.easyshell.sqlite.record.SQLiteInsertRecordParam;
import cn.oyzh.easyshell.sqlite.record.SQLiteRecord;
import cn.oyzh.easyshell.sqlite.record.SQLiteRecordPrimaryKey;
import cn.oyzh.easyshell.sqlite.record.SQLiteSelectRecordParam;
import cn.oyzh.easyshell.sqlite.record.SQLiteUpdateRecordParam;
import cn.oyzh.easyshell.trees.sqlite.root.ShellSqliteRootTreeItem;
import cn.oyzh.easyshell.util.sqlite.ShellSqliteViewFactory;
import cn.oyzh.fx.db.DBObjectList;
import cn.oyzh.fx.db.DBRecordData;
import cn.oyzh.fx.db.listener.DBStatusListener;
import cn.oyzh.fx.db.listener.DBStatusListenerManager;
import cn.oyzh.fx.db.ui.DBStatusColumn;
import cn.oyzh.fx.db.util.DBUtil;
import cn.oyzh.fx.gui.tabs.RichTabController;
import cn.oyzh.fx.plus.controls.box.FXVBox;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * SQLite 查询结果标签页控制器
 *
 * @author oyzh
 * @since 2026-11-02
 */
public class ShellSqliteQuerySelectTabController extends RichTabController {

    /**
     * 根节点
     */
    @FXML
    private FXVBox root;

    /**
     * SQL组件
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
    private ShellSqliteRecordTableView recordTable;

    /**
     * 数据库树节点
     */
    private ShellSqliteRootTreeItem dbItem;

    /**
     * 执行结果
     */
    private SQLiteExecuteResult result;

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
    private SQLiteColumns columns;

    /**
     * 执行初始化
     *
     * @param result 执行结果
     * @param dbItem 数据库树节点
     */
    public void init(SQLiteExecuteResult result, ShellSqliteRootTreeItem dbItem) {
        this.result = result;
        this.dbItem = dbItem;
        if (result.isUpdatable()) {
            if (this.changeListener == null) {
                this.changeListener = new DBStatusListener(this.result.dbName() + ":" + this.result.tableName()) {
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
    private void initColumns(List<SQLiteColumn> columns) {
        // 设置字段列表
        this.columns = new SQLiteColumns(columns);
        // 数据列集合
        List<FXTableColumn<SQLiteRecord, Object>> columnList = new ArrayList<>();
        DBStatusColumn<SQLiteRecord> statusColumn = new DBStatusColumn<>();
        columnList.add(statusColumn);
        for (SQLiteColumn column : columns) {
            ShellSqliteRecordColumn tableColumn = new ShellSqliteRecordColumn(column, false);
            tableColumn.setPrefWidth(DBUtil.suitableColumnWidth(column));
            columnList.add(tableColumn);
        }
        this.recordTable.setColumn(columnList);
    }

    /**
     * 初始化记录
     *
     * @param records 数据
     */
    private void initRecords(List<SQLiteRecord> records) {
        this.recordTable.setItem(records);
    }

    /**
     * 添加记录
     */
    @FXML
    private void addRecord() {
        SQLiteRecord record = new SQLiteRecord(new SQLiteColumns(this.columns));
        record.setCreated(true);
        for (SQLiteColumn column : this.columns) {
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
    private void insertRecord(SQLiteRecord record) {
        DBRecordData recordData = record.getRecordData();
        SQLiteRecordPrimaryKey primaryKey = this.initPrimaryKey(record);
        SQLiteInsertRecordParam param = new SQLiteInsertRecordParam();
        param.setRecord(recordData);
        param.setPrimaryKey(primaryKey);
        param.setDbName(this.result.dbName());
        param.setTableName(this.result.tableName());
        this.dbItem.client().insertRecord(param);
        if (primaryKey != null) {
            SQLiteSelectRecordParam selectRecordParam = new SQLiteSelectRecordParam();
            selectRecordParam.setPrimaryKey(primaryKey);
            selectRecordParam.setDbName(this.result.dbName());
            selectRecordParam.setTableName(this.result.tableName());
            selectRecordParam.setColumns(this.result.columnList());
            SQLiteRecord newRecord = this.dbItem.client().selectRecord(selectRecordParam);
            if (newRecord != null) {
                record.copy(newRecord);
            }
        }
    }

    /**
     * 更改记录
     *
     * @param record 记录
     */
    private void updateRecord(SQLiteRecord record) {
        SQLiteRecordPrimaryKey primaryKey = this.initPrimaryKey(record);
        SQLiteUpdateRecordParam param = new SQLiteUpdateRecordParam();
        param.setDbName(this.result.dbName());
        param.setTableName(this.result.tableName());
        if (primaryKey != null) {
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
            SQLiteSelectRecordParam selectRecordParam = new SQLiteSelectRecordParam();
            selectRecordParam.setPrimaryKey(primaryKey);
            selectRecordParam.setDbName(this.result.dbName());
            selectRecordParam.setTableName(this.result.tableName());
            selectRecordParam.setColumns(this.result.columnList());
            // 处理回显
            SQLiteRecord newRecord = this.dbItem.client().selectRecord(selectRecordParam);
            if (newRecord != null) {
                record.copy(newRecord);
            }
        } else {// 主键不存在，则根据所有字段更新
            // 变更数据
            DBRecordData changedRecordData = record.getChangedRecordData();
            // 原始数据
            DBRecordData originalRecordData = record.getOriginalRecordData();
            param.setRecord(originalRecordData);
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
    private SQLiteRecordPrimaryKey initPrimaryKey(SQLiteRecord record) {
        SQLiteColumn primaryKeyColumn = this.result.getPrimaryKey();
        if (primaryKeyColumn != null) {
            SQLiteRecordPrimaryKey primaryKey = new SQLiteRecordPrimaryKey();
            primaryKey.init(primaryKeyColumn, record);
            return primaryKey;
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
                List<SQLiteRecord> records = this.recordTable.getItems();
                for (SQLiteRecord record : records) {
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
            SQLiteRecord discardRecord = null;
            for (SQLiteRecord record : this.recordTable.getItems()) {
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
            SQLiteDataExportTable exportTable = new SQLiteDataExportTable();
            exportTable.setSelected(true);
            exportTable.setName("query_result");
            exportTable.columns(this.result.getColumns());
            exportTable.setRecords(this.result.getRecords().stream().map(record -> {
                Map<String, Object> values = new LinkedHashMap<>();
                for (SQLiteColumn column : this.result.getColumns()) {
                    values.put(column.getName(), record.getValue(column));
                }
                return values;
            }).toList());
            ShellSqliteViewFactory.exportData(this.dbItem.client(), null, 1, exportTable);
        } catch (Exception ex) {
            MessageBox.exception(ex, this.getWindow());
        }
    }

    /**
     * 删除记录
     */
    @FXML
    private void deleteRecord() {
        List<SQLiteRecord> records = new ArrayList<>(this.recordTable.getSelectedItems());
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
    private void deleteRecords(List<SQLiteRecord> records) {
        try {
            boolean success = false;
            for (SQLiteRecord record : records) {
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
    private boolean deleteRecord(SQLiteRecord record) {
        // 如果是新增的数据，直接删除
        boolean success;
        if (record.isCreated()) {
            success = true;
        } else {
            // 获取主键
            SQLiteRecordPrimaryKey primaryKey = this.initPrimaryKey(record);
            SQLiteDeleteRecordParam param = new SQLiteDeleteRecordParam();
            param.setDbName(this.result.dbName());
            param.setTableName(this.result.tableName());
            param.setPrimaryKey(primaryKey);
            param.setRecord(record.getOriginalRecordData());
            success = this.dbItem.client().deleteRecord(param) == 1;
        }
        if (success) {
            record.destroy();
        }
        return success;
    }

    @Override
    public void onTabClosed(Event event) {
        super.onTabClosed(event);
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
        this.recordTable.getItems().addListener((ListChangeListener<SQLiteRecord>) c -> {
            if (c.next() && c.wasAdded()) {
                List<? extends SQLiteRecord> rows = c.getAddedSubList();
                for (SQLiteRecord row : rows) {
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
