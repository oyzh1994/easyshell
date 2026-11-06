package cn.oyzh.easyshell.trees.postgresql.table;

import cn.oyzh.common.dto.Paging;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.postgresql.ShellPostgresqlClient;
import cn.oyzh.easyshell.postgresql.check.PostgresqlCheck;
import cn.oyzh.easyshell.postgresql.column.PostgresqlColumn;
import cn.oyzh.easyshell.postgresql.column.PostgresqlColumns;
import cn.oyzh.easyshell.postgresql.column.PostgresqlSelectColumnParam;
import cn.oyzh.easyshell.postgresql.foreignKey.PostgresqlForeignKey;
import cn.oyzh.easyshell.postgresql.index.PostgresqlIndex;
import cn.oyzh.easyshell.postgresql.record.PostgresqlDeleteRecordParam;
import cn.oyzh.easyshell.postgresql.record.PostgresqlInsertRecordParam;
import cn.oyzh.easyshell.postgresql.record.PostgresqlRecord;
import cn.oyzh.easyshell.postgresql.record.PostgresqlRecordFilter;
import cn.oyzh.easyshell.postgresql.record.PostgresqlRecordPrimaryKey;
import cn.oyzh.easyshell.postgresql.record.PostgresqlSelectRecordParam;
import cn.oyzh.easyshell.postgresql.record.PostgresqlUpdateRecordParam;
import cn.oyzh.easyshell.postgresql.table.PostgresqlTable;
import cn.oyzh.easyshell.postgresql.trigger.PostgresqlTrigger;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.event.postgresql.ShellPostgresqlEventUtil;
import cn.oyzh.easyshell.trees.postgresql.ShellPostgresqlTreeItem;
import cn.oyzh.easyshell.trees.postgresql.schema.ShellPostgresqlSchemaTreeItem;
import cn.oyzh.easyshell.util.postgresql.ShellPostgresqlViewFactory;
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
 * PostgreSQL数据库树表节点
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class ShellPostgresqlTableTreeItem extends ShellPostgresqlTreeItem<ShellPostgresqlTableTreeItemValue> {

    /**
     * 当前值
     */
    private final PostgresqlTable value;

    /**
     * 构造PostgreSQL数据库树表节点
     *
     * @param table    表
     * @param treeView 树视图
     */
    public ShellPostgresqlTableTreeItem(PostgresqlTable table, RichTreeView treeView) {
        super(treeView);
        this.value = table;
        this.setValue(new ShellPostgresqlTableTreeItemValue(this));
    }

    @Override
    public ShellPostgresqlTablesTreeItem parent() {
        return (ShellPostgresqlTablesTreeItem) super.parent();
    }

    /**
     * 获取PostgreSQL数据库客户端
     *
     * @return PostgreSQL数据库客户端
     */
    public ShellPostgresqlClient client() {
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
            PostgresqlTable postgresqlTable = this.dbItem().selectTable(cloneTable);
            this.dbItem().getTableTypeChild().addTable(postgresqlTable);
        } catch (Exception ex) {
            MessageBox.exception(ex, this.getWindow());
        }
    }

    /**
     * 转储
     */
    private void dump() {
        ShellPostgresqlViewFactory.dumpData(this.client(), this.schema(), this.tableName(), 2);
    }

    /**
     * 导出
     */
    private void export() {
        ShellPostgresqlViewFactory.exportData(this.client(), this.schema(), this.tableName());
    }

    /**
     * 设计表
     */
    private void designTable() {
        this.reloadChild();
        ShellPostgresqlEventUtil.designTable(this.value, this.dbItem());
    }

    /**
     * 截断表
     */
    private void truncateTable() {
        if (MessageBox.confirm(ShellI18nHelper.confirm("truncateTable", this.tableName()), this.getWindow())) {
            try {
                this.dbItem().truncateTable(this.tableName());
                ShellPostgresqlEventUtil.tableTruncated(this, this.dbItem());
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
            if (MessageBox.confirm(ShellI18nHelper.confirm("clearTableData", this.tableName()), this.getWindow())) {
                this.dbItem().clearTable(this.tableName());
                ShellPostgresqlEventUtil.tableCleared(this, this.dbItem());
            }
        } catch (Exception ex) {
            MessageBox.exception(ex, this.getWindow());
        }
    }

    @Override
    public void delete() {
        try {
            if (MessageBox.confirm(ShellI18nHelper.confirm("deleteTable", this.tableName()), this.getWindow())) {
                this.dbItem().dropTable(this.tableName());
                ShellPostgresqlEventUtil.tableDropped(this, this.dbItem());
                this.parent().clearTableSize();
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
        ShellPostgresqlViewFactory.tableInfo(this);
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
            ShellPostgresqlEventUtil.tableRenamed(oldName, tableName, this.dbItem());
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
    public ShellPostgresqlSchemaTreeItem dbItem() {
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
    public Paging<PostgresqlRecord> recordPage(long pageNo, long limit, List<PostgresqlRecordFilter> filters, List<PostgresqlColumn> columns) {
        PostgresqlSelectRecordParam param = new PostgresqlSelectRecordParam();
        param.setLimit(limit);
        param.setFilters(filters);
        param.setColumns(columns);
        param.setSchema(this.schema());
        param.setStart(pageNo * limit);
        param.setTableName(this.tableName());
        List<PostgresqlRecord> rows = this.client().selectRecords(param);
        long count = this.client().selectRecordCount(param);
        Paging<PostgresqlRecord> paging = new Paging<>(rows, limit, count);
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
    public List<PostgresqlColumn> columns() {
        return this.client().selectColumns(new PostgresqlSelectColumnParam(this.schema(), this.tableName()));
    }

    /**
     * 查询索引
     *
     * @return 索引列表
     */
    public List<PostgresqlIndex> indexes() {
        return this.client().indexes(this.schema(), this.tableName());
    }

    /**
     * 查询检查约束
     *
     * @return 检查约束列表
     */
    public List<PostgresqlCheck> checks() {
        return this.client().selectChecks(this.schema(), this.tableName());
    }

    /**
     * 查询外键
     *
     * @return 外键列表
     */
    public List<PostgresqlForeignKey> foreignKeys() {
        return this.client().selectForeignKeys(this.schema(), this.tableName());
    }

    /**
     * 查询触发器
     *
     * @return 触发器列表
     */
    public List<PostgresqlTrigger> triggers() {
        return this.client().selectTriggers(this.schema(), this.tableName());
    }

    @Override
    public void onPrimaryDoubleClick() {
        ShellPostgresqlEventUtil.tableOpen(this, this.dbItem());
    }

    /**
     * 列
     */
    private PostgresqlColumns columns;

    /**
     * 获取主键列，优先返回自动递增列
     *
     * @return 主键列
     */
    public PostgresqlColumn getPrimaryKey() {
        if (this.columns == null) {
            this.columns = new PostgresqlColumns(this.columns());
        }
        PostgresqlColumn dbColumn = null;
        for (PostgresqlColumn column : this.columns.primaryKeys()) {
            if (column.isAutoIncrement()) {
                dbColumn = column;
                break;
            }
        }
        if (dbColumn == null) {
            for (PostgresqlColumn column : this.columns.primaryKeys()) {
                return column;
            }
        }
        return dbColumn;
    }

    @Override
    public void loadChild() {
        try {
            PostgresqlTable table = this.client().selectTable(this.schema(), this.tableName());
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
            this.columns = new PostgresqlColumns(this.columns());
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
    public int insertRecord(DBRecordData recordData, PostgresqlRecordPrimaryKey primaryKey) {
        PostgresqlInsertRecordParam param = new PostgresqlInsertRecordParam();
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
        PostgresqlDeleteRecordParam param = new PostgresqlDeleteRecordParam();
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
    public int deleteRecord(PostgresqlRecordPrimaryKey primaryKey) {
        PostgresqlDeleteRecordParam param = new PostgresqlDeleteRecordParam();
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
    public PostgresqlRecord selectRecord(PostgresqlRecordPrimaryKey primaryKey) {
        PostgresqlSelectRecordParam param = new PostgresqlSelectRecordParam();
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
    public int updateRecord(DBRecordData recordData, PostgresqlRecordPrimaryKey primaryKey) {
        PostgresqlUpdateRecordParam param = new PostgresqlUpdateRecordParam();
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
        PostgresqlUpdateRecordParam param = new PostgresqlUpdateRecordParam();
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
    public PostgresqlTable value() {
        return value;
    }
}
