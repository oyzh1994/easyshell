package cn.oyzh.easyshell.trees.sqlserver.table;

import cn.oyzh.common.dto.Paging;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.sqlserver.ShellSqlServerClient;
import cn.oyzh.easyshell.sqlserver.check.SqlServerCheck;
import cn.oyzh.easyshell.sqlserver.column.SqlServerColumn;
import cn.oyzh.easyshell.sqlserver.column.SqlServerColumns;
import cn.oyzh.easyshell.sqlserver.column.SqlServerSelectColumnParam;
import cn.oyzh.easyshell.sqlserver.foreignKey.SqlServerForeignKey;
import cn.oyzh.easyshell.sqlserver.index.SqlServerIndex;
import cn.oyzh.easyshell.sqlserver.record.SqlServerDeleteRecordParam;
import cn.oyzh.easyshell.sqlserver.record.SqlServerInsertRecordParam;
import cn.oyzh.easyshell.sqlserver.record.SqlServerRecord;
import cn.oyzh.easyshell.sqlserver.record.SqlServerRecordFilter;
import cn.oyzh.easyshell.sqlserver.record.SqlServerRecordPrimaryKey;
import cn.oyzh.easyshell.sqlserver.record.SqlServerSelectRecordParam;
import cn.oyzh.easyshell.sqlserver.record.SqlServerUpdateRecordParam;
import cn.oyzh.easyshell.sqlserver.table.SqlServerTable;
import cn.oyzh.easyshell.sqlserver.trigger.SqlServerTrigger;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.event.sqlserver.ShellSqlServerEventUtil;
import cn.oyzh.easyshell.trees.sqlserver.ShellSqlServerTreeItem;
import cn.oyzh.easyshell.trees.sqlserver.schema.ShellSqlServerSchemaTreeItem;
import cn.oyzh.easyshell.util.sqlserver.ShellSqlServerViewFactory;
import cn.oyzh.easyshell.util.db.ShellDB18nHelper;
import cn.oyzh.fx.db.DBRecordData;
import cn.oyzh.fx.db.util.DBUtil;
import cn.oyzh.fx.gui.menu.MenuItemHelper;
import cn.oyzh.fx.gui.svg.glyph.CopySVGGlyph;
import cn.oyzh.fx.gui.tree.view.RichTreeView;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.menu.FXMenuItem;
import cn.oyzh.fx.plus.window.StageManager;
import cn.oyzh.easyshell.util.ShellI18nHelper;
import cn.oyzh.i18n.I18nHelper;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * SqlServer数据库树表节点
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class ShellSqlServerTableTreeItem extends ShellSqlServerTreeItem<ShellSqlServerTableTreeItemValue> {

    /**
     * 当前值
     */
    private final SqlServerTable value;

    /**
     * 构造SqlServer数据库树表节点
     *
     * @param table    表
     * @param treeView 树视图
     */
    public ShellSqlServerTableTreeItem(SqlServerTable table, RichTreeView treeView) {
        super(treeView);
        this.value = table;
        this.setValue(new ShellSqlServerTableTreeItemValue(this));
    }

    @Override
    public ShellSqlServerTablesTreeItem parent() {
        return (ShellSqlServerTablesTreeItem) super.parent();
    }

    /**
     * 获取SqlServer数据库客户端
     *
     * @return SqlServer数据库客户端
     */
    public ShellSqlServerClient client() {
        return this.parent().client();
    }

    /**
     * 获取模式名称
     *
     * @return 模式名称
     */
    public String schema() {
        return this.parent().schema();
    }

    /**
     * 获取表名称
     *
     * @return 表名称
     */
    public String tableName() {
        return this.value.getName();
    }

    /**
     * 获取连接信息
     *
     * @return 连接信息
     */
    public ShellConnect info() {
        return this.parent().info();
    }

    @Override
    public List<MenuItem> getMenuItems() {
        List<MenuItem> items = new ArrayList<>();
        FXMenuItem openTable = MenuItemHelper.openTable(this::onPrimaryDoubleClick);
        items.add(openTable);
        FXMenuItem updateTable = MenuItemHelper.designTable(this::designTable);
        items.add(updateTable);
        FXMenuItem renameTable = MenuItemHelper.renameTable(this::rename);
        items.add(renameTable);
        FXMenuItem clearTable = MenuItemHelper.clearTable(this::clearTable);
        items.add(clearTable);
        FXMenuItem truncateTable = MenuItemHelper.truncateTable(this::truncateTable);
        items.add(truncateTable);
        FXMenuItem dropTable = MenuItemHelper.deleteTable(this::delete);
        items.add(dropTable);
        items.add(MenuItemHelper.separator());
        FXMenuItem dumpTable = MenuItemHelper.dumpData(this::dump);
        items.add(dumpTable);
        FXMenuItem exportTable = MenuItemHelper.exportData(this::export);
        items.add(exportTable);

        // 克隆表
        Menu cloneTable = MenuItemHelper.menu(I18nHelper.cloneTable(), new CopySVGGlyph());
        MenuItem clone1 = MenuItemHelper.menuItem(ShellDB18nHelper.tableTip3(), () -> this.cloneTable(true));
        MenuItem clone2 = MenuItemHelper.menuItem(ShellDB18nHelper.tableTip4(), () -> this.cloneTable(false));
        cloneTable.getItems().addAll(clone1, clone2);
        items.add(cloneTable);

        MenuItem tableInfo = MenuItemHelper.tableInfo(this::tableInfo);
        items.add(tableInfo);

        return items;
    }

    /**
     * 克隆表
     *
     * @param includeRecord 是否包含记录
     */
    private void cloneTable(boolean includeRecord) {
        StageManager.showMask(this.getWindow(), () -> this.doCloneTable(includeRecord));
    }

    /**
     * 执行克隆表
     *
     * @param includeRecord 是否包含记录
     */
    private void doCloneTable(boolean includeRecord) {
        try {
            String cloneTable = this.tableName() + DBUtil.genCloneName();
            this.dbItem().cloneTable(this.tableName(), cloneTable, includeRecord);
            SqlServerTable sqlserverTable = this.dbItem().selectTable(cloneTable);
            this.dbItem().getTableTypeChild().addTable(sqlserverTable);
        } catch (Exception ex) {
            MessageBox.exception(ex, this.getWindow());
        }
    }

    /**
     * 转储
     */
    private void dump() {
        ShellSqlServerViewFactory.dumpData(this.client(), this.schema(), this.tableName(), 2);
    }

    /**
     * 导出
     */
    private void export() {
        ShellSqlServerViewFactory.exportData(this.client(), this.schema(), this.tableName());
    }

    /**
     * 设计表
     */
    private void designTable() {
        ShellSqlServerEventUtil.designTable(this.value, this.dbItem());
    }

    /**
     * 截断表
     */
    private void truncateTable() {
        if (MessageBox.confirm(ShellI18nHelper.confirmTruncateTable(this.tableName()), this.getWindow())) {
            try {
                this.dbItem().truncateTable(this.tableName());
                ShellSqlServerEventUtil.tableTruncated(this, this.dbItem());
            } catch (Exception ex) {
                ex.printStackTrace();
                MessageBox.exception(ex, this.getWindow());
            }
        }
    }

    /**
     * 清空表
     */
    private void clearTable() {
        try {
            if (MessageBox.confirm(ShellI18nHelper.confirmClearTableData(this.tableName()), this.getWindow())) {
                this.dbItem().clearTable(this.tableName());
                ShellSqlServerEventUtil.tableCleared(this, this.dbItem());
            }
        } catch (Exception ex) {
            MessageBox.exception(ex, this.getWindow());
        }
    }

    @Override
    public void delete() {
        try {
            if (MessageBox.confirm(ShellI18nHelper.confirmDeleteTable(this.tableName()), this.getWindow())) {
                this.dbItem().dropTable(this.tableName());
                ShellSqlServerEventUtil.tableDropped(this, this.dbItem());
//                this.parent().clearTableSize();
                this.remove();
            }
        } catch (Exception ex) {
            MessageBox.exception(ex, this.getWindow());
        }
    }

    /**
     * 表信息
     */
    private void tableInfo() {
        ShellSqlServerViewFactory.tableInfo(this);
    }

    @Override
    public void rename() {
        try {
            String tableName = MessageBox.prompt(I18nHelper.pleaseInputName(), this.value.getName(), this.getWindow());
            // 名称为null或者跟当前名称相同，则忽略
            if (tableName == null || Objects.equals(tableName, this.value.getName())) {
                return;
            }
            // 检查名称
            if (StringUtil.isBlank(tableName)) {
                MessageBox.warn(I18nHelper.pleaseInputContent(), this.getWindow());
                return;
            }
            String oldName = this.value.getName();
            // 修改名称
            this.dbItem().renameTable(oldName, tableName);
            this.value.setName(tableName);
            this.refresh();
            ShellSqlServerEventUtil.tableRenamed(oldName, tableName, this.dbItem());
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex, this.getWindow());
        }
    }

    /**
     * 获取所属模式节点
     *
     * @return 模式节点
     */
    public ShellSqlServerSchemaTreeItem dbItem() {
        if (this.parent() == null) {
            return null;
        }
        return this.parent().parent();
    }

    /**
     * 分页查询记录
     *
     * @param pageNo  页码
     * @param limit   每页数量
     * @param filters 过滤条件
     * @param columns 列
     * @return 分页数据
     */
    public Paging<SqlServerRecord> recordPage(long pageNo, long limit, List<SqlServerRecordFilter> filters, List<SqlServerColumn> columns) {
        SqlServerSelectRecordParam param = new SqlServerSelectRecordParam();
        param.setLimit(limit);
        param.setFilters(filters);
        param.setColumns(columns);
        param.setSchema(this.schema());
        param.setStart(pageNo * limit);
        param.setTableName(this.tableName());
        List<SqlServerRecord> rows = this.client().selectRecords(param);
        long count = this.client().selectRecordCount(param);
        Paging<SqlServerRecord> paging = new Paging<>(rows, limit, count);
        paging.currentPage(pageNo);
        return paging;
    }

    /**
     * 获取连接名称
     *
     * @return 连接名称
     */
    public String infoName() {
        return parent().infoName();
    }

    /**
     * 查询列
     *
     * @return 列列表
     */
    public List<SqlServerColumn> columns() {
        return this.client().selectColumns(new SqlServerSelectColumnParam(this.schema(), this.tableName()));
    }

    /**
     * 查询索引
     *
     * @return 索引列表
     */
    public List<SqlServerIndex> indexes() {
        return this.client().indexes(this.schema(), this.tableName());
    }

    /**
     * 查询检查约束
     *
     * @return 检查约束列表
     */
    public List<SqlServerCheck> checks() {
        return this.client().selectChecks(this.schema(), this.tableName());
    }

    /**
     * 查询外键
     *
     * @return 外键列表
     */
    public List<SqlServerForeignKey> foreignKeys() {
        return this.client().selectForeignKeys(this.schema(), this.tableName());
    }

    /**
     * 查询触发器
     *
     * @return 触发器列表
     */
    public List<SqlServerTrigger> triggers() {
        return this.client().selectTriggers(this.schema(), this.tableName());
    }

    @Override
    public void onPrimaryDoubleClick() {
        ShellSqlServerEventUtil.tableOpen(this, this.dbItem());
    }

    /**
     * 列
     */
    private SqlServerColumns columns;

    /**
     * 获取主键列，优先返回自动递增列
     *
     * @return 主键列
     */
    public SqlServerColumn getPrimaryKey() {
        if (this.columns == null) {
            this.columns = new SqlServerColumns(this.columns());
        }
        SqlServerColumn dbColumn = null;
        for (SqlServerColumn column : this.columns.primaryKeys()) {
            if (column.isAutoIncrement()) {
                dbColumn = column;
                break;
            }
        }
        if (dbColumn == null) {
            for (SqlServerColumn column : this.columns.primaryKeys()) {
                return column;
            }
        }
        return dbColumn;
    }

    @Override
    public void loadChild() {
        try {
            SqlServerTable table = this.client().selectTableSimple(this.schema(), this.tableName());
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

    /**
     * 判断是否存在主键
     *
     * @return 是否存在
     */
    public boolean hasPrimaryKey() {
        if (this.columns == null) {
            this.columns = new SqlServerColumns(this.columns());
        }
        return !this.columns.primaryKeys().isEmpty();
    }

    /**
     * 新增记录
     *
     * @param recordData 记录数据
     * @return 影响行数
     */
    public int insertRecord(DBRecordData recordData) {
        return this.insertRecord(recordData, null);
    }

    /**
     * 新增记录
     *
     * @param recordData 记录数据
     * @param primaryKey 主键
     * @return 影响行数
     */
    public int insertRecord(DBRecordData recordData, SqlServerRecordPrimaryKey primaryKey) {
        SqlServerInsertRecordParam param = new SqlServerInsertRecordParam();
        param.setRecord(recordData);
        param.setSchema(this.schema());
        param.setPrimaryKey(primaryKey);
        param.setTableName(this.tableName());
        return this.client().insertRecord(param);
    }

    /**
     * 删除记录
     *
     * @param recordData 记录数据
     * @return 影响行数
     */
    public int deleteRecord(DBRecordData recordData) {
        SqlServerDeleteRecordParam param = new SqlServerDeleteRecordParam();
        param.setSchema(this.schema());
        param.setTableName(this.tableName());
        param.setRecord(recordData);
        return this.client().deleteRecord(param);
    }

    /**
     * 删除记录
     *
     * @param primaryKey 主键
     * @return 影响行数
     */
    public int deleteRecord(SqlServerRecordPrimaryKey primaryKey) {
        SqlServerDeleteRecordParam param = new SqlServerDeleteRecordParam();
        param.setSchema(this.schema());
        param.setTableName(this.tableName());
        param.setPrimaryKey(primaryKey);
        return this.client().deleteRecord(param);
    }

    /**
     * 查询记录
     *
     * @param primaryKey 主键
     * @return 记录
     */
    public SqlServerRecord selectRecord(SqlServerRecordPrimaryKey primaryKey) {
        SqlServerSelectRecordParam param = new SqlServerSelectRecordParam();
        param.setSchema(this.schema());
        param.setTableName(this.tableName());
        param.setPrimaryKey(primaryKey);
        return this.client().selectRecord(param);
    }

    /**
     * 修改记录
     *
     * @param recordData 记录数据
     * @param primaryKey 主键
     * @return 影响行数
     */
    public int updateRecord(DBRecordData recordData, SqlServerRecordPrimaryKey primaryKey) {
        SqlServerUpdateRecordParam param = new SqlServerUpdateRecordParam();
        param.setSchema(this.schema());
        param.setTableName(this.tableName());
        param.setPrimaryKey(primaryKey);
        param.setUpdateRecord(recordData);
        return this.client().updateRecord(param);
    }

    /**
     * 修改记录
     *
     * @param recordData         记录数据
     * @param originalRecordData 原始记录数据
     * @return 影响行数
     */
    public int updateRecord(DBRecordData recordData, DBRecordData originalRecordData) {
        SqlServerUpdateRecordParam param = new SqlServerUpdateRecordParam();
        param.setSchema(this.schema());
        param.setTableName(this.tableName());
        param.setUpdateRecord(recordData);
        param.setRecord(originalRecordData);
        return this.client().updateRecord(param);
    }

    /**
     * 获取表值
     *
     * @return 表值
     */
    public SqlServerTable value() {
        return value;
    }
}
