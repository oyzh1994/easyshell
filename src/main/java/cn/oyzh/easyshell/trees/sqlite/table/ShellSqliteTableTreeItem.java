package cn.oyzh.easyshell.trees.sqlite.table;

import cn.oyzh.common.dto.Paging;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.event.sqlite.ShellSqliteEventUtil;
import cn.oyzh.easyshell.sqlite.column.SQLiteColumn;
import cn.oyzh.easyshell.sqlite.column.SQLiteColumns;
import cn.oyzh.easyshell.sqlite.record.SQLiteDeleteRecordParam;
import cn.oyzh.easyshell.sqlite.record.SQLiteInsertRecordParam;
import cn.oyzh.easyshell.sqlite.record.SQLiteRecord;
import cn.oyzh.easyshell.sqlite.record.SQLiteRecordFilter;
import cn.oyzh.easyshell.sqlite.record.SQLiteRecordPrimaryKey;
import cn.oyzh.easyshell.sqlite.record.SQLiteSelectRecordParam;
import cn.oyzh.easyshell.sqlite.record.SQLiteUpdateRecordParam;
import cn.oyzh.easyshell.sqlite.table.SQLiteTable;
import cn.oyzh.easyshell.sqlite.ShellSqliteClient;
import cn.oyzh.easyshell.trees.sqlite.ShellSqliteTreeItem;
import cn.oyzh.easyshell.trees.sqlite.root.ShellSqliteRootTreeItem;
import cn.oyzh.easyshell.util.sqlite.ShellSqliteViewFactory;
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
 * SQLite表节点
 *
 * @author oyzh
 * @since 2026-11-02
 */
public class ShellSqliteTableTreeItem extends ShellSqliteTreeItem<ShellSqliteTableTreeItemValue> {

    /**
     * 当前值
     */
    private final SQLiteTable value;

    /**
     * 字段缓存
     */
    private SQLiteColumns columns;

    /**
     * 构造表节点
     *
     * @param table    表对象
     * @param treeView 树视图
     */
    public ShellSqliteTableTreeItem(SQLiteTable table, RichTreeView treeView) {
        super(treeView);
        this.value = table;
        this.setValue(new ShellSqliteTableTreeItemValue(this));
    }

    @Override
    public ShellSqliteTablesTreeItem parent() {
        return (ShellSqliteTablesTreeItem) super.parent();
    }

    /**
     * 获取数据库树节点
     *
     * @return 数据库树节点
     */
    public ShellSqliteRootTreeItem dbItem() {
        return this.parent().parent();
    }

    /**
     * 获取表对象
     *
     * @return 表对象
     */
    public SQLiteTable value() {
        return this.value;
    }

    /**
     * 获取SQLite客户端
     *
     * @return SQLite客户端
     */
    public ShellSqliteClient client() {
        return this.parent().client();
    }

    /**
     * 获取数据库名称
     *
     * @return 数据库名称
     */
    public String dbName() {
        return this.parent().dbName();
    }

    /**
     * 获取连接信息
     *
     * @return 连接信息
     */
    public ShellConnect info() {
        return this.parent().info();
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
     * 获取连接名称
     *
     * @return 连接名称
     */
    public String infoName() {
        return this.parent().infoName();
    }

    /**
     * 获取字段列表
     *
     * @return 字段列表
     */
    public SQLiteColumns columns() {
        if (this.columns == null) {
            this.columns = new SQLiteColumns(this.client().selectColumns(this.tableName()));
        }
        return this.columns;
    }

    /**
     * 清空字段缓存
     */
    public void clearColumns() {
        this.columns = null;
    }

    /**
     * 获取主键列，优先返回自动递增列
     *
     * @return 主键列
     */
    public SQLiteColumn getPrimaryKey() {
        List<SQLiteColumn> primaryKeys = this.columns().primaryKeys();
        return primaryKeys.isEmpty() ? null : primaryKeys.getFirst();
    }

    /**
     * 分页查询表记录
     *
     * @param pageNo  页码
     * @param limit   每页大小
     * @param filters 过滤条件
     * @param columns 列信息
     * @return 分页结果
     */
    public Paging<SQLiteRecord> recordPage(long pageNo, long limit, List<SQLiteRecordFilter> filters,
                                           List<SQLiteColumn> columns) {
        SQLiteSelectRecordParam param = new SQLiteSelectRecordParam();
        param.setLimit(limit);
        param.setStart(pageNo * limit);
        param.setFilters(filters);
        param.setColumns(columns);
        param.setDbName(this.dbName());
        param.setTableName(this.tableName());
        List<SQLiteRecord> records = this.client().selectRecords(param);
        long count = this.client().selectRecordCount(param);
        Paging<SQLiteRecord> paging = new Paging<>(records, limit, count);
        paging.currentPage(pageNo);
        return paging;
    }

    /**
     * 新增表记录
     *
     * @param recordData 记录数据
     * @return 受影响行数
     */
    public int insertRecord(DBRecordData recordData) {
        return this.insertRecord(recordData, null);
    }

    /**
     * 新增表记录
     *
     * @param recordData 记录数据
     * @param primaryKey 主键
     * @return 受影响行数
     */
    public int insertRecord(DBRecordData recordData, SQLiteRecordPrimaryKey primaryKey) {
        SQLiteInsertRecordParam param = new SQLiteInsertRecordParam();
        param.setRecord(recordData);
        param.setDbName(this.dbName());
        param.setPrimaryKey(primaryKey);
        param.setTableName(this.tableName());
        return this.client().insertRecord(param);
    }

    /**
     * 删除表记录
     *
     * @param recordData 记录数据
     * @return 受影响行数
     */
    public int deleteRecord(DBRecordData recordData) {
        SQLiteDeleteRecordParam param = new SQLiteDeleteRecordParam();
        param.setDbName(this.dbName());
        param.setTableName(this.tableName());
        param.setRecord(recordData);
        return this.client().deleteRecord(param);
    }

    /**
     * 删除表记录
     *
     * @param primaryKey 主键
     * @return 受影响行数
     */
    public int deleteRecord(SQLiteRecordPrimaryKey primaryKey) {
        SQLiteDeleteRecordParam param = new SQLiteDeleteRecordParam();
        param.setDbName(this.dbName());
        param.setTableName(this.tableName());
        param.setPrimaryKey(primaryKey);
        return this.client().deleteRecord(param);
    }

    /**
     * 查询单条表记录
     *
     * @param primaryKey 主键
     * @return 表记录
     */
    public SQLiteRecord selectRecord(SQLiteRecordPrimaryKey primaryKey) {
        SQLiteSelectRecordParam param = new SQLiteSelectRecordParam();
        param.setDbName(this.dbName());
        param.setTableName(this.tableName());
        param.setColumns(this.columns());
        param.setPrimaryKey(primaryKey);
        return this.client().selectRecord(param);
    }

    /**
     * 更新表记录
     *
     * @param recordData 记录数据
     * @param primaryKey 主键
     * @return 受影响行数
     */
    public int updateRecord(DBRecordData recordData, SQLiteRecordPrimaryKey primaryKey) {
        SQLiteUpdateRecordParam param = new SQLiteUpdateRecordParam();
        param.setDbName(this.dbName());
        param.setTableName(this.tableName());
        param.setPrimaryKey(primaryKey);
        param.setUpdateRecord(recordData);
        return this.client().updateRecord(param);
    }

    /**
     * 更新表记录
     *
     * @param recordData         记录数据
     * @param originalRecordData 原始记录数据
     * @return 受影响行数
     */
    public int updateRecord(DBRecordData recordData, DBRecordData originalRecordData) {
        SQLiteUpdateRecordParam param = new SQLiteUpdateRecordParam();
        param.setDbName(this.dbName());
        param.setTableName(this.tableName());
        param.setUpdateRecord(recordData);
        param.setRecord(originalRecordData);
        return this.client().updateRecord(param);
    }

    @Override
    public void onPrimaryDoubleClick() {
        ShellSqliteEventUtil.tableOpen(this.client(), this.tableName());
    }

    @Override
    public List<MenuItem> getMenuItems() {
        List<MenuItem> items = new ArrayList<>();
        FXMenuItem openTable = MenuItemHelper.openTable(this::onPrimaryDoubleClick);
        FXMenuItem designTable = MenuItemHelper.designTable(this::design);
        FXMenuItem renameTable = MenuItemHelper.renameTable(this::rename);
        FXMenuItem clearTable = MenuItemHelper.clearTable(this::clearTable);
        FXMenuItem truncateTable = MenuItemHelper.truncateTable(this::truncateTable);
        FXMenuItem deleteTable = MenuItemHelper.deleteTable(this::delete);
        items.add(openTable);
        items.add(designTable);
        items.add(renameTable);
        items.add(clearTable);
        items.add(truncateTable);
        items.add(deleteTable);
        items.add(MenuItemHelper.separator());
        FXMenuItem dumpTable = MenuItemHelper.dumpData(this::dumpData);
        FXMenuItem exportTable = MenuItemHelper.exportData(this::exportData);
        items.add(dumpTable);
        items.add(exportTable);
        // 克隆表
        Menu cloneTable = MenuItemHelper.menu(I18nHelper.cloneTable(), new CopySVGGlyph());
        cloneTable.getItems().add(MenuItemHelper.menuItem(I18nHelper.dataAndStructure(), () -> this.cloneTable(true)));
        cloneTable.getItems().add(MenuItemHelper.menuItem(I18nHelper.structure(), () -> this.cloneTable(false)));
        items.add(cloneTable);
        FXMenuItem info = MenuItemHelper.tableInfo(this::tableInfo);
        items.add(info);
        return items;
    }

    /**
     * 设计表
     */
    private void design() {
        ShellSqliteEventUtil.designTable(this.client(), this.tableName());
    }

    /**
     * 表信息
     */
    private void tableInfo() {
        ShellSqliteViewFactory.tableInfo(this);
    }

    /**
     * 导出数据
     */
    private void exportData() {
        ShellSqliteViewFactory.exportData(this.client(), this.tableName());
    }

    /**
     * 转储数据
     */
    private void dumpData() {
        ShellSqliteViewFactory.dumpData(this.client(), this.tableName(), 2, 0);
    }

    @Override
    public void rename() {
        String name = MessageBox.prompt(I18nHelper.pleaseInputName(), this.tableName(), this.getWindow());
        // 名称为null或者跟当前名称相同，则忽略
        if (name == null || Objects.equals(name, this.tableName())) {
            return;
        }
        // 检查名称
        if (StringUtil.isBlank(name)) {
            MessageBox.warn(I18nHelper.pleaseInputContent(), this.getWindow());
            return;
        }
        try {
            String oldName = this.tableName();
            // 修改名称
            this.client().renameTable(oldName, name);
            this.value.setName(name);
            this.clearColumns();
            this.refresh();
            this.parent().doSort();
            this.parent().clearTableSize();
            ShellSqliteEventUtil.tableChanged(this.client(), oldName, name);
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex, this.getWindow());
        }
    }

    @Override
    public void delete() {
        if (!MessageBox.confirm(ShellI18nHelper.confirm("deleteTable", this.tableName()), this.getWindow())) {
            return;
        }
        try {
            this.client().dropTable(this.tableName());
            this.parent().clearTableSize();
            ShellSqliteEventUtil.tableChanged(this.client(), this.tableName(), null);
            this.remove();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex, this.getWindow());
        }
    }

    /**
     * 清空表
     */
    private void clearTable() {
        if (!MessageBox.confirm(ShellI18nHelper.confirm("clearTable", this.tableName()), this.getWindow())) {
            return;
        }
        try {
            this.client().clearTable(this.tableName());
            ShellSqliteEventUtil.tableChanged(this.client(), this.tableName(), this.tableName());
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex, this.getWindow());
        }
    }

    /**
     * 清空表并重置自增
     */
    private void truncateTable() {
        if (!MessageBox.confirm(ShellI18nHelper.confirm("truncateTable", this.tableName()), this.getWindow())) {
            return;
        }
        try {
            this.client().truncateTable(this.tableName());
            ShellSqliteEventUtil.tableChanged(this.client(), this.tableName(), this.tableName());
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex, this.getWindow());
        }
    }

    /**
     * 克隆表
     *
     * @param includeRecords 是否包含记录
     */
    private void cloneTable(boolean includeRecords) {
        StageManager.showMask(this.getWindow(), () -> {
            try {
                String cloneName = this.tableName() + DBUtil.genCloneName();
                this.client().cloneTable(this.tableName(), cloneName, includeRecords);
                ShellSqliteEventUtil.tableChanged(this.client(), null, cloneName);
            } catch (Exception ex) {
                ex.printStackTrace();
                MessageBox.exception(ex, this.getWindow());
            }
        });
    }
}
