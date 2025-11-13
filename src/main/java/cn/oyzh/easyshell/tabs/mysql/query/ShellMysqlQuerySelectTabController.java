package cn.oyzh.easyshell.tabs.mysql.query;

import cn.oyzh.easyshell.domain.ShellQuery;
import cn.oyzh.easyshell.fx.db.DBStatusColumn;
import cn.oyzh.easyshell.fx.mysql.data.ShellMysqlDataExportTable;
import cn.oyzh.easyshell.fx.mysql.record.ShellMysqlRecordColumn;
import cn.oyzh.easyshell.fx.mysql.record.ShellMysqlRecordTableView;
import cn.oyzh.easyshell.db.DBObjectList;
import cn.oyzh.easyshell.db.listener.DBStatusListener;
import cn.oyzh.easyshell.db.listener.DBStatusListenerManager;
import cn.oyzh.easyshell.mysql.column.MysqlColumn;
import cn.oyzh.easyshell.mysql.query.MysqlExecuteResult;
import cn.oyzh.easyshell.mysql.record.MysqlDeleteRecordParam;
import cn.oyzh.easyshell.mysql.record.MysqlInsertRecordParam;
import cn.oyzh.easyshell.mysql.record.MysqlRecord;
import cn.oyzh.easyshell.mysql.record.MysqlRecordData;
import cn.oyzh.easyshell.mysql.record.MysqlRecordPrimaryKey;
import cn.oyzh.easyshell.mysql.record.MysqlSelectRecordParam;
import cn.oyzh.easyshell.mysql.record.MysqlUpdateRecordParam;
import cn.oyzh.easyshell.trees.mysql.database.ShellMysqlDatabaseTreeItem;
import cn.oyzh.easyshell.util.mysql.ShellMysqlRecordUtil;
import cn.oyzh.easyshell.util.mysql.ShellMysqlViewFactory;
import cn.oyzh.fx.gui.tabs.RichTabController;
import cn.oyzh.fx.plus.controls.box.FXVBox;
import cn.oyzh.fx.plus.controls.svg.SVGGlyph;
import cn.oyzh.fx.plus.controls.tab.FXTabPane;
import cn.oyzh.fx.plus.controls.table.FXTableColumn;
import cn.oyzh.fx.plus.controls.text.FXText;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.node.NodeGroupUtil;
import cn.oyzh.fx.plus.node.NodeUtil;
import cn.oyzh.i18n.I18nHelper;
import javafx.beans.value.ObservableValue;
import javafx.collections.ListChangeListener;
import javafx.event.Event;
import javafx.fxml.FXML;

import java.util.ArrayList;
import java.util.List;

/**
 * @author oyzh
 * @since 2024/08/12
 */
public class ShellMysqlQuerySelectTabController extends RichTabController {

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
    private ShellMysqlRecordTableView recordTable;

    /**
     * 数据库树节点
     */
    private ShellMysqlDatabaseTreeItem dbItem;

    /**
     * 执行结果
     */
    private MysqlExecuteResult result;

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
    private List<MysqlColumn> columns;

    /**
     * 执行初始化
     *
     * @param result 执行结果
     * @param dbItem db树表节点
     */
    public void init(MysqlExecuteResult result, ShellMysqlDatabaseTreeItem dbItem) {
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
            this.sql.setText(this.result.getSql());
            this.used.setText(I18nHelper.time() + ": " + this.result.getUsedMs() + "ms");
            // this.count.setText(I18nHelper.totalData() + ": " + this.result.getCount());
            // 初始化计数
            this.initCount(this.result.getCount());
        } catch (Exception ex) {
            MessageBox.exception(ex);
        }
    }

    /**
     * 初始化计数
     *
     * @param count 计数
     */
    private void initCount(int count) {
        this.count.text(I18nHelper.totalData() + ": " + count);
    }

    /**
     * 初始化列
     *
     * @param columns 列数据
     */
    private void initColumns(List<MysqlColumn> columns) {
        // 设置字段列表
        this.columns = columns;
        // 数据列集合
        List<FXTableColumn<MysqlRecord, Object>> columnList = new ArrayList<>();
        DBStatusColumn<MysqlRecord> statusColumn = new DBStatusColumn<>();
        columnList.add(statusColumn);
        for (MysqlColumn column : columns) {
            ShellMysqlRecordColumn tableColumn = new ShellMysqlRecordColumn(column, false);
            tableColumn.setPrefWidth(ShellMysqlRecordUtil.suitableColumnWidth(column));
            columnList.add(tableColumn);
        }
        this.recordTable.getColumns().setAll(columnList);
    }

    /**
     * 初始化记录
     *
     * @param records 数据
     */
    private void initRecords(List<MysqlRecord> records) {
        this.recordTable.setItem(records);
    }

    /**
     * 添加记录
     */
    @FXML
    private void addRecord() {
        MysqlRecord record = new MysqlRecord(this.columns);
        record.setCreated(true);
        for (MysqlColumn column : this.columns) {
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
    private void insertRecord(MysqlRecord record) {
        MysqlRecordData recordData = record.getRecordData();
        MysqlRecordPrimaryKey primaryKey = this.initPrimaryKey(record);
        MysqlInsertRecordParam param = new MysqlInsertRecordParam();
        param.setRecord(recordData);
        param.setPrimaryKey(primaryKey);
        param.setDbName(this.result.dbName());
        param.setTableName(this.result.tableName());
        this.dbItem.client().insertRecord(param);
        if (primaryKey != null) {
            MysqlSelectRecordParam selectRecordParam = new MysqlSelectRecordParam();
            selectRecordParam.setPrimaryKey(primaryKey);
            selectRecordParam.setDbName(this.result.dbName());
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
    private void updateRecord(MysqlRecord record) {
        // 获取主键
        MysqlRecordPrimaryKey primaryKey = this.initPrimaryKey(record);
        MysqlUpdateRecordParam param = new MysqlUpdateRecordParam();
        param.setDbName(this.result.dbName());
        param.setTableName(this.result.tableName());
        // 主键存在，则根据主键更新
        if (primaryKey != null) {
            // 记录数据
            MysqlRecordData recordData = record.getChangedRecordData();
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
            MysqlSelectRecordParam selectRecordParam = new MysqlSelectRecordParam();
            selectRecordParam.setPrimaryKey(primaryKey);
            selectRecordParam.setDbName(this.result.dbName());
            selectRecordParam.setTableName(this.result.tableName());
            // 处理回显
            record.copy(this.dbItem.selectRecord(selectRecordParam));
        } else {// 主键不存在，则根据所有字段更新
            // 变更数据
            MysqlRecordData changedRecordData = record.getChangedRecordData();
            // 原始数据
            MysqlRecordData originalRecordData = record.getOriginalRecordData();
            param.setUpdateRecord(originalRecordData);
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
    private MysqlRecordPrimaryKey initPrimaryKey(MysqlRecord record) {
        MysqlColumn primaryKeyColumn = this.result.getPrimaryKey();
        if (primaryKeyColumn != null) {
            MysqlRecordPrimaryKey primaryKey = new MysqlRecordPrimaryKey();
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
                List<MysqlRecord> records = this.recordTable.getItems();
                for (MysqlRecord record : records) {
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
                MessageBox.exception(ex);
            }
        }
    }

    /**
     * 丢弃变更
     */
    @FXML
    private void discard() {
        try {
            MysqlRecord discardRecord = null;
            for (MysqlRecord record : this.recordTable.getItems()) {
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
            MessageBox.exception(ex);
        }
    }

    /**
     * 刷新记录
     */
    @FXML
    private void reload() {
        try {
            // 检查是否有未保存的数据
            if (this.apply.isEnable() && !MessageBox.confirm(I18nHelper.unsavedAndContinue())) {
                return;
            }
            // 执行查询
            this.result = this.dbItem.executeSingleSql(this.result.getSql());
            // 初始化数据
            this.initDataList();
            // 禁用组件
            this.apply.disable();
        } catch (Exception ex) {
            MessageBox.exception(ex);
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
            ShellMysqlDataExportTable exportTable = new ShellMysqlDataExportTable();
            exportTable.setSelected(true);
            exportTable.setName(query.getName());
            exportTable.columns(this.result.getColumns());
            exportTable.setRecords(this.result.getRecords());
            ShellMysqlViewFactory.exportData(this.dbItem.client(), this.result.dbName(), null, 1, exportTable);
        } catch (Exception ex) {
            MessageBox.exception(ex);
        }
    }

    /**
     * 删除记录
     */
    @FXML
    private void deleteRecord() {
        // try {
        //     MysqlRecord record = this.recordTable.getSelectedItem();
        //     if (record == null) {
        //         return;
        //     }
        //     if (!MessageBox.confirm(I18nHelper.deleteRecord() + "?")) {
        //         return;
        //     }
        //     // 如果是新增的数据，直接删除
        //     boolean success;
        //     if (record.isCreated()) {
        //         success = true;
        //     } else {
        //         // 获取主键
        //         MysqlRecordPrimaryKey primaryKey = this.initPrimaryKey(record);
        //         MysqlDeleteRecordParam param = new MysqlDeleteRecordParam();
        //         param.setDbName(this.result.dbName());
        //         param.setTableName(this.result.tableName());
        //         param.setPrimaryKey(primaryKey);
        //         param.setRecord(record.getOriginalRecordData());
        //         success = this.dbItem.deleteRecord(param) == 1;
        //     }
        //     // 操作成功
        //     if (success) {
        //         this.recordTable.removeItem(record);
        //     } else {// 操作失败
        //         MessageBox.warnToast(I18nHelper.operationFail());
        //     }
        // } catch (Exception ex) {
        //     MessageBox.exception(ex);
        // }
        List<MysqlRecord> records = this.recordTable.getSelectedItems();
        if (!MessageBox.confirm(I18nHelper.deleteRecord() + "?")) {
            return;
        }
        try {
            boolean success = false;
            for (MysqlRecord record : records) {
                success = this.doDeleteRecord(record);
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
                MessageBox.warnToast(I18nHelper.operationFail());
            }
        } catch (Exception ex) {
            MessageBox.exception(ex);
        }
    }

    /**
     * 删除表记录
     *
     * @param record 表记录
     * @return 结果
     */
    private boolean doDeleteRecord(MysqlRecord record) {
        // 如果是新增的数据，直接删除
        boolean success;
        if (record.isCreated()) {
            success = true;
        } else {
            // 获取主键
            MysqlRecordPrimaryKey primaryKey = this.initPrimaryKey(record);
            MysqlDeleteRecordParam param = new MysqlDeleteRecordParam();
            param.setDbName(this.result.dbName());
            param.setTableName(this.result.tableName());
            param.setPrimaryKey(primaryKey);
            param.setRecord(record.getOriginalRecordData());
            success = this.dbItem.deleteRecord(param) == 1;
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
        this.recordTable.getItems().addListener((ListChangeListener<MysqlRecord>) c -> {
            if (c.next() && c.wasAdded()) {
                List<? extends MysqlRecord> rows = c.getAddedSubList();
                for (MysqlRecord row : rows) {
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

    // @Override
    // public void initialize(URL url, ResourceBundle resourceBundle) {
    //     try {
    //         super.initialize(url, resourceBundle);
    //         // this.add.managedBindVisible();
    //         // this.delete.managedBindVisible();
    //         this.discard.disableProperty().bind(this.apply.disableProperty());
    //         this.apply.disabledProperty().addListener((observable, oldValue, newValue) -> {
    //             if (newValue) {
    //                 NodeGroupUtil.enable(this.root, "action2");
    //             } else {
    //                 NodeGroupUtil.disable(this.root, "action2");
    //             }
    //         });
    //         this.recordTable.getItems().addListener((ListChangeListener<MysqlRecord>) c -> {
    //             if (c.next() && c.wasAdded()) {
    //                 List<? extends MysqlRecord> rows = c.getAddedSubList();
    //                 for (MysqlRecord row : rows) {
    //                     if (DBObjectList.isCreated(row)) {
    //                         this.apply.enable();
    //                         break;
    //                     }
    //                 }
    //             }
    //         });
    //         this.recordTable.setCtrlSAction(this::apply);
    //         NodeUtil.nodeOnCtrlS(this.root, this::apply);
    //     } catch (Exception ex) {
    //         ex.printStackTrace();
    //     }
    // }

    // /**
    //  * 删除记录
    //  */
    // @EventSubscribe
    // private void deleteRecord(RecordDeleteEvent event) {
    //     if (this.recordTable.hasRecord(event.data())) {
    //         this.doDeleteRecord(event.data());
    //     }
    // }
}
