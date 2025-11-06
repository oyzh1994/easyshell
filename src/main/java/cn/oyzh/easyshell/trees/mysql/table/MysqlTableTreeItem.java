package cn.oyzh.easyshell.trees.mysql.table;

import cn.oyzh.common.dto.Paging;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.controller.mysql.data.MysqlDataDumpController;
import cn.oyzh.easyshell.controller.mysql.data.MysqlDataExportController;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.event.mysql.MysqlEventUtil;
import cn.oyzh.easyshell.mysql.MysqlClient;
import cn.oyzh.easyshell.mysql.check.MysqlChecks;
import cn.oyzh.easyshell.mysql.column.MysqlColumn;
import cn.oyzh.easyshell.mysql.column.MysqlColumns;
import cn.oyzh.easyshell.mysql.column.MysqlSelectColumnParam;
import cn.oyzh.easyshell.mysql.foreignKey.MysqlForeignKey;
import cn.oyzh.easyshell.mysql.index.MysqlIndex;
import cn.oyzh.easyshell.mysql.record.MysqlDeleteRecordParam;
import cn.oyzh.easyshell.mysql.record.MysqlInsertRecordParam;
import cn.oyzh.easyshell.mysql.record.MysqlRecord;
import cn.oyzh.easyshell.mysql.record.MysqlRecordData;
import cn.oyzh.easyshell.mysql.record.MysqlRecordFilter;
import cn.oyzh.easyshell.mysql.record.MysqlRecordPrimaryKey;
import cn.oyzh.easyshell.mysql.record.MysqlSelectRecordParam;
import cn.oyzh.easyshell.mysql.record.MysqlUpdateRecordParam;
import cn.oyzh.easyshell.mysql.table.MysqlSelectTableParam;
import cn.oyzh.easyshell.mysql.table.MysqlTable;
import cn.oyzh.easyshell.mysql.trigger.MysqlTrigger;
import cn.oyzh.easyshell.trees.mysql.DBTreeItem;
import cn.oyzh.easyshell.trees.mysql.database.MysqlDatabaseTreeItem;
import cn.oyzh.easyshell.util.mysql.DBI18nHelper;
import cn.oyzh.fx.gui.menu.MenuItemHelper;
import cn.oyzh.fx.gui.svg.glyph.CopySVGGlyph;
import cn.oyzh.fx.gui.tree.view.RichTreeView;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.menu.FXMenuItem;
import cn.oyzh.fx.plus.window.StageAdapter;
import cn.oyzh.fx.plus.window.StageManager;
import cn.oyzh.i18n.I18nHelper;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * db树表节点
 *
 * @author oyzh
 * @since 2023/12/27
 */
public class MysqlTableTreeItem extends DBTreeItem<MysqlTableTreeItemValue> {

    /**
     * 当前值
     */
    private final MysqlTable value;

    public MysqlTableTreeItem(MysqlTable table, RichTreeView treeView) {
        super(treeView);
        this.value = table;
        this.setValue(new MysqlTableTreeItemValue(this));
    }

    @Override
    public MysqlTablesTreeItem parent() {
        return (MysqlTablesTreeItem) super.parent();
    }

    public MysqlClient client() {
        return this.parent().client();
    }

    public String dbName() {
        return this.parent().dbName();
    }

    public String tableName() {
        return this.value.getName();
    }

    /**
     * 获取redis信息
     *
     * @return redis信息
     */
    public ShellConnect info() {
        return this.parent().info();
    }

    @Override
    public List<MenuItem> getMenuItems() {
        List<MenuItem> items = new ArrayList<>();
        FXMenuItem openTable = MenuItemHelper.openTable("12", this::onPrimaryDoubleClick);
        items.add(openTable);
        FXMenuItem updateTable = MenuItemHelper.designTable("12", this::designTable);
        items.add(updateTable);
        FXMenuItem renameTable = MenuItemHelper.renameTable("12", this::rename);
        items.add(renameTable);
        FXMenuItem clearTable = MenuItemHelper.clearTableData("12", this::clearTableData);
        items.add(clearTable);
        FXMenuItem truncateTable = MenuItemHelper.truncateTable("12", this::truncateTable);
        items.add(truncateTable);
        FXMenuItem dropTable = MenuItemHelper.deleteTable("12", this::delete);
        items.add(dropTable);
        items.add(MenuItemHelper.separator());
        FXMenuItem dumpTable = MenuItemHelper.dumpData("12", this::dump);
        items.add(dumpTable);
        FXMenuItem exportTable = MenuItemHelper.exportData("12", this::export);
        items.add(exportTable);
        // FXMenuItem tableInfo = MenuItemHelper.tableInfo("12", this::tableInfo);
        // items.add(tableInfo);

        // 克隆表
        Menu cloneTable = MenuItemHelper.menu(I18nHelper.cloneTable(), new CopySVGGlyph("12"));
        MenuItem clone1 = MenuItemHelper.menuItem(DBI18nHelper.tableTip3(), () -> this.cloneTable(true));
        MenuItem clone2 = MenuItemHelper.menuItem(DBI18nHelper.tableTip4(), () -> this.cloneTable(false));
        cloneTable.getItems().addAll(clone1, clone2);

        items.add(cloneTable);
        return items;
    }

    /**
     * 克隆表
     *
     * @param includeRecord 是否包含记录
     */
    private void cloneTable(boolean includeRecord) {
        StageManager.showMask(() -> this.doCloneTable(includeRecord));
    }

    /**
     * 执行克隆表
     *
     * @param includeRecord 是否包含记录
     */
    private void doCloneTable(boolean includeRecord) {
        try {
            String cloneTable = this.dbItem().cloneTable(this.tableName(), includeRecord);
            MysqlTable mysqlTable = this.dbItem().selectTable(cloneTable);
            this.dbItem().getTableTypeChild().addTable(mysqlTable);
        } catch (Exception ex) {
            MessageBox.exception(ex);
        }
    }

    /**
     * 转储
     */
    private void dump() {
        StageAdapter fxView = StageManager.parseStage(MysqlDataDumpController.class, this.window());
        fxView.setProp("dumpType", 2);
        fxView.setProp("dbInfo", this.info());
        fxView.setProp("dbName", this.dbName());
        fxView.setProp("dbClient", this.client());
        fxView.setProp("tableName", this.tableName());
        fxView.display();
    }

    /**
     * 导出
     */
    private void export() {
        StageAdapter fxView = StageManager.parseStage(MysqlDataExportController.class, this.window());
        fxView.setProp("dumpType", 2);
        fxView.setProp("dbInfo", this.info());
        fxView.setProp("dbName", this.dbName());
        fxView.setProp("dbClient", this.client());
        fxView.setProp("tableName", this.tableName());
        fxView.display();
    }

    private void designTable() {
        this.reloadChild();
        MysqlEventUtil.designTable(this.value, this.dbItem());
    }

    private void truncateTable() {
        if (MessageBox.confirm(I18nHelper.truncateTable() + "[" + this.tableName() + "]")) {
            try {
                this.dbItem().truncateTable(this.tableName());
                MysqlEventUtil.tableTruncated(this, this.dbItem());
            } catch (Exception ex) {
                ex.printStackTrace();
                MessageBox.exception(ex);
            }
        }
    }

    /**
     * 清空表
     */
    private void clearTableData() {
        try {
            if (MessageBox.confirm(I18nHelper.clearTableData() + "[" + this.tableName() + "]")) {
                this.dbItem().clearTable(this.tableName());
                MysqlEventUtil.tableCleared(this, this.dbItem());
            }
        } catch (Exception ex) {
            MessageBox.exception(ex);
        }
    }

    @Override
    public void delete() {
        try {
            if (MessageBox.confirm(I18nHelper.deleteTable() + "[" + this.tableName() + "]")) {
                this.dbItem().dropTable(this.tableName());
                MysqlEventUtil.tableDropped(this, this.dbItem());
                this.remove();
            } else {
                MessageBox.warn(I18nHelper.operationFail());
            }
        } catch (Exception ex) {
            MessageBox.exception(ex);
        }
    }

    // private void tableInfo() {
    //     StageAdapter fxView = StageManager.parseStage(MysqlTableInfoController.class, this.window());
    //     fxView.setProp("tableItem", this);
    //     fxView.display();
    // }

    @Override
    public void rename() {
        try {
            // if (!MessageBox.confirm(DBI18nHelper.tableTip2())) {
            //     return;
            // }
            String tableName = MessageBox.prompt(I18nHelper.pleaseInputName(), this.value.getName());
            // 名称为null或者跟当前名称相同，则忽略
            if (tableName == null || Objects.equals(tableName, this.value.getName())) {
                return;
            }
            // 检查名称
            if (StringUtil.isBlank(tableName)) {
                MessageBox.warn(I18nHelper.pleaseInputContent());
                return;
            }
            // if (this.dbItem().existTable(tableName)) {
            //     MessageBox.warn(I18nHelper.table() + " " + tableName + I18nHelper.alreadyExists());
            //     return;
            // }
            String oldName = this.value.getName();
            // 修改名称
            this.dbItem().renameTable(oldName, tableName);
            this.value.setName(tableName);
            this.refresh();
            MysqlEventUtil.tableRenamed(this, this.dbItem());
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }

    public MysqlDatabaseTreeItem dbItem() {
        if (this.parent() == null) {
            return null;
        }
        return this.parent().parent();
    }

    public Paging<MysqlRecord> recordPage(long pageNo, long limit, List<MysqlRecordFilter> filters, List<MysqlColumn> columns) {
        MysqlSelectRecordParam param = new MysqlSelectRecordParam();
        param.setLimit(limit);
        param.setFilters(filters);
        param.setColumns(columns);
        param.setDbName(this.dbName());
        param.setStart(pageNo * limit);
        param.setTableName(this.tableName());
        List<MysqlRecord> rows = this.client().selectRecords(param);
        long count = this.client().selectRecordCount(param);
        Paging<MysqlRecord> paging = new Paging<>(rows, limit, count);
        paging.currentPage(pageNo);
        return paging;
    }

    public String infoName() {
        return parent().infoName();
    }

    public MysqlColumns columns() {
        return this.client().selectColumns(new MysqlSelectColumnParam(this.dbName(), this.tableName()));
    }

    public List<MysqlIndex> indexes() {
        return this.client().indexes(this.dbName(), this.tableName());
    }

    public MysqlChecks checks() {
        return this.client().checks(this.dbName(), this.tableName());
    }

    public List<MysqlForeignKey> foreignKeys() {
        return this.client().foreignKeys(this.dbName(), this.tableName());
    }

    public List<MysqlTrigger> triggers() {
        return this.client().triggers(this.dbName(), this.tableName());
    }

    @Override
    public void onPrimaryDoubleClick() {
        MysqlEventUtil.tableOpen(this, this.dbItem());
    }

    private MysqlColumns columns;

    /**
     * 获取主键列，优先返回自动递增列
     *
     * @return 主键列
     */
    public MysqlColumn getPrimaryKey() {
        if (columns == null) {
            columns = this.columns();
        }
        MysqlColumn dbColumn = null;
        for (MysqlColumn column : this.columns.primaryKeys()) {
            if (column.isAutoIncrement()) {
                dbColumn = column;
                break;
            }
        }
        if (dbColumn == null) {
            for (MysqlColumn column : this.columns.primaryKeys()) {
                return column;
            }
        }
        return dbColumn;
    }

    @Override
    public void loadChild() {
        try {
            MysqlSelectTableParam param = new MysqlSelectTableParam();
            param.setFull(true);
            param.setDbName(this.dbName());
            param.setTableName(this.tableName());
            MysqlTable table = this.client().selectTable(param);
            if (table != null) {
                this.value.copy(table);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void reloadChild() {
        this.clearChild();
        this.setLoaded(false);
        this.loadChild();
    }

    public boolean hasPrimaryKey() {
        if (columns == null) {
            columns = this.columns();
        }
        return this.columns.primaryKeys().isEmpty();
    }

    public int insertRecord(MysqlRecordData recordData) {
        return this.insertRecord(recordData, null);
    }

    public int insertRecord(MysqlRecordData recordData, MysqlRecordPrimaryKey primaryKey) {
        MysqlInsertRecordParam param = new MysqlInsertRecordParam();
        param.setRecord(recordData);
        param.setDbName(this.dbName());
        param.setPrimaryKey(primaryKey);
        param.setTableName(this.tableName());
        return this.client().insertRecord(param);
    }

    public int deleteRecord(MysqlRecordData recordData) {
        MysqlDeleteRecordParam param = new MysqlDeleteRecordParam();
        param.setDbName(this.dbName());
        param.setTableName(this.tableName());
        param.setRecord(recordData);
        return this.client().deleteRecord(param);
    }

    public int deleteRecord(MysqlRecordPrimaryKey primaryKey) {
        MysqlDeleteRecordParam param = new MysqlDeleteRecordParam();
        param.setDbName(this.dbName());
        param.setTableName(this.tableName());
        param.setPrimaryKey(primaryKey);
        return this.client().deleteRecord(param);
    }

    public MysqlRecord selectRecord(MysqlRecordPrimaryKey primaryKey) {
        MysqlSelectRecordParam param = new MysqlSelectRecordParam();
        param.setDbName(this.dbName());
        param.setTableName(this.tableName());
        param.setPrimaryKey(primaryKey);
        return this.client().selectRecord(param);
    }

    public int updateRecord(MysqlRecordData recordData, MysqlRecordPrimaryKey primaryKey) {
        MysqlUpdateRecordParam param = new MysqlUpdateRecordParam();
        param.setDbName(this.dbName());
        param.setTableName(this.tableName());
        param.setPrimaryKey(primaryKey);
        param.setUpdateRecord(recordData);
        return this.client().updateRecord(param);
    }

    public int updateRecord(MysqlRecordData recordData, MysqlRecordData originalRecordData) {
        MysqlUpdateRecordParam param = new MysqlUpdateRecordParam();
        param.setDbName(this.dbName());
        param.setTableName(this.tableName());
        param.setUpdateRecord(recordData);
        param.setRecord(originalRecordData);
        return this.client().updateRecord(param);
    }

    public MysqlTable value() {
        return value;
    }
}
