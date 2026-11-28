package cn.oyzh.easyshell.tabs.h2.query;

import cn.oyzh.common.util.TextUtil;
import cn.oyzh.easyshell.data.h2.dto.H2DataExportTable;
import cn.oyzh.easyshell.fx.h2.record.ShellH2RecordColumn;
import cn.oyzh.easyshell.fx.h2.record.ShellH2RecordTableView;
import cn.oyzh.easyshell.query.h2.H2ExecuteResult;
import cn.oyzh.easyshell.h2.column.H2Column;
import cn.oyzh.easyshell.h2.column.H2Columns;
import cn.oyzh.easyshell.h2.record.H2DeleteRecordParam;
import cn.oyzh.easyshell.h2.record.H2InsertRecordParam;
import cn.oyzh.easyshell.h2.record.H2Record;
import cn.oyzh.easyshell.h2.record.H2RecordPrimaryKey;
import cn.oyzh.easyshell.h2.record.H2SelectRecordParam;
import cn.oyzh.easyshell.h2.record.H2UpdateRecordParam;
import cn.oyzh.easyshell.trees.h2.root.ShellH2RootTreeItem;
import cn.oyzh.easyshell.util.h2.ShellH2ViewFactory;
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
 * H2 查询结果标签页控制器
 *
 * @author oyzh
 * @since 2026-11-02
 */
public class ShellH2QuerySelectTabController extends RichTabController {

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
    private ShellH2RecordTableView recordTable;

    /**
     * 数据库树节点
     */
    private ShellH2RootTreeItem dbItem;

    /**
     * 执行结果
     */
    private H2ExecuteResult result;

    /**
     * 模式
     */
    private String schema;

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
    private H2Columns columns;

    /**
     * 执行初始化
     *
     * @param result 执行结果
     * @param dbItem 数据库树节点
     */
    public void init(H2ExecuteResult result, ShellH2RootTreeItem dbItem) {
        this.init(result, dbItem, dbItem.client().schema());
    }

    public void init(H2ExecuteResult result, ShellH2RootTreeItem dbItem, String schema) {
        this.result = result;
        this.dbItem = dbItem;
        this.schema = dbItem.client().schema(schema);
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
    private void initColumns(List<H2Column> columns) {
        // 设置字段列表
        this.columns = new H2Columns(columns);
        // 数据列集合
        List<FXTableColumn<H2Record, Object>> columnList = new ArrayList<>();
        DBStatusColumn<H2Record> statusColumn = new DBStatusColumn<>();
        columnList.add(statusColumn);
        for (H2Column column : columns) {
            ShellH2RecordColumn tableColumn = new ShellH2RecordColumn(column);
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
    private void initRecords(List<H2Record> records) {
        this.recordTable.setItem(records);
    }

    /**
     * 添加记录
     */
    @FXML
    private void addRecord() {
        H2Record record = new H2Record(new H2Columns(this.columns));
        record.setCreated(true);
        for (H2Column column : this.columns) {
            record.putValue(column, null);
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
    private void insertRecord(H2Record record) {
        DBRecordData recordData = record.getRecordData();
        H2RecordPrimaryKey primaryKey = this.initPrimaryKey(record);
        H2InsertRecordParam param = new H2InsertRecordParam();
        param.setRecord(recordData);
        param.setPrimaryKey(primaryKey);
        param.setDbName(this.result.dbName());
        param.setTableName(this.result.tableName());
        this.dbItem.client().insertRecord(param);
        if (primaryKey != null) {
            H2SelectRecordParam selectRecordParam = new H2SelectRecordParam();
            selectRecordParam.setPrimaryKey(primaryKey);
            selectRecordParam.setDbName(this.result.dbName());
            selectRecordParam.setTableName(this.result.tableName());
            selectRecordParam.setColumns(this.result.columnList());
            H2Record newRecord = this.dbItem.client().selectRecord(selectRecordParam);
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
    private void updateRecord(H2Record record) {
        H2RecordPrimaryKey primaryKey = this.initPrimaryKey(record);
        H2UpdateRecordParam param = new H2UpdateRecordParam();
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
            H2SelectRecordParam selectRecordParam = new H2SelectRecordParam();
            selectRecordParam.setPrimaryKey(primaryKey);
            selectRecordParam.setDbName(this.result.dbName());
            selectRecordParam.setTableName(this.result.tableName());
            selectRecordParam.setColumns(this.result.columnList());
            // 处理回显
            H2Record newRecord = this.dbItem.client().selectRecord(selectRecordParam);
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
    private H2RecordPrimaryKey initPrimaryKey(H2Record record) {
        H2Column primaryKeyColumn = this.result.getPrimaryKey();
        if (primaryKeyColumn != null) {
            H2RecordPrimaryKey primaryKey = new H2RecordPrimaryKey();
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
                List<H2Record> records = this.recordTable.getItems();
                for (H2Record record : records) {
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
            H2Record discardRecord = null;
            for (H2Record record : this.recordTable.getItems()) {
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
            this.result = this.dbItem.executeSingleSql(
                    this.schema, this.result.getContent());
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
            H2DataExportTable exportTable = new H2DataExportTable();
            exportTable.setSelected(true);
            exportTable.setName("query_result");
            exportTable.columns(this.result.getColumns());
            exportTable.setRecords(this.result.getRecords().stream().map(record -> {
                Map<String, Object> values = new LinkedHashMap<>();
                for (H2Column column : this.result.getColumns()) {
                    values.put(column.getName(), record.getValue(column));
                }
                return values;
            }).toList());
            ShellH2ViewFactory.exportData(
                    this.dbItem.client(), this.schema, null, 1, exportTable);
        } catch (Exception ex) {
            MessageBox.exception(ex, this.getWindow());
        }
    }

    /**
     * 删除记录
     */
    @FXML
    private void deleteRecord() {
        List<H2Record> records = new ArrayList<>(this.recordTable.getSelectedItems());
        if (!MessageBox.confirm(ShellI18nHelper.confirmDeleteRecord(), this.getWindow())) {
            return;
        }
        StageManager.showMask(this.getWindow(), () -> this.deleteRecords(records));
    }

    /**
     * 删除记录
     *
     * @param records 记录
     */
    private void deleteRecords(List<H2Record> records) {
        try {
            boolean success = false;
            for (H2Record record : records) {
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
    private boolean deleteRecord(H2Record record) {
        // 如果是新增的数据，直接删除
        boolean success;
        if (record.isCreated()) {
            success = true;
        } else {
            // 获取主键
            H2RecordPrimaryKey primaryKey = this.initPrimaryKey(record);
            H2DeleteRecordParam param = new H2DeleteRecordParam();
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
        this.recordTable.getItems().addListener((ListChangeListener<H2Record>) c -> {
            if (c.next() && c.wasAdded()) {
                List<? extends H2Record> rows = c.getAddedSubList();
                for (H2Record row : rows) {
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
