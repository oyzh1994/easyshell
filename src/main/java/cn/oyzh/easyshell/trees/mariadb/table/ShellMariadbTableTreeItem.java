package cn.oyzh.easyshell.trees.mariadb.table;

import cn.oyzh.common.dto.Paging;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.event.mariadb.ShellMariadbEventUtil;
import cn.oyzh.easyshell.mariadb.ShellMariadbClient;
import cn.oyzh.easyshell.mariadb.check.MariadbCheck;
import cn.oyzh.easyshell.mariadb.column.MariadbColumn;
import cn.oyzh.easyshell.mariadb.column.MariadbColumns;
import cn.oyzh.easyshell.mariadb.column.MariadbSelectColumnParam;
import cn.oyzh.easyshell.mariadb.foreignKey.MariadbForeignKey;
import cn.oyzh.easyshell.mariadb.index.MariadbIndex;
import cn.oyzh.easyshell.mariadb.record.MariadbDeleteRecordParam;
import cn.oyzh.easyshell.mariadb.record.MariadbInsertRecordParam;
import cn.oyzh.easyshell.mariadb.record.MariadbRecord;
import cn.oyzh.easyshell.mariadb.record.MariadbRecordFilter;
import cn.oyzh.easyshell.mariadb.record.MariadbRecordPrimaryKey;
import cn.oyzh.easyshell.mariadb.record.MariadbSelectRecordParam;
import cn.oyzh.easyshell.mariadb.record.MariadbUpdateRecordParam;
import cn.oyzh.easyshell.mariadb.table.MariadbSelectTableParam;
import cn.oyzh.easyshell.mariadb.table.MariadbTable;
import cn.oyzh.easyshell.mariadb.trigger.MariadbTrigger;
import cn.oyzh.easyshell.trees.mariadb.ShellMariadbTreeItem;
import cn.oyzh.easyshell.trees.mariadb.database.ShellMariadbDatabaseTreeItem;
import cn.oyzh.easyshell.util.db.ShellDB18nHelper;
import cn.oyzh.easyshell.util.mariadb.ShellMariadbViewFactory;
import cn.oyzh.fx.db.DBObjects;
import cn.oyzh.fx.db.DBRecordData;
import cn.oyzh.fx.db.util.DBUtil;
import cn.oyzh.fx.gui.menu.MenuItemHelper;
import cn.oyzh.fx.gui.svg.glyph.CopySVGGlyph;
import cn.oyzh.fx.gui.tree.view.RichTreeView;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.menu.FXMenuItem;
import cn.oyzh.fx.plus.window.StageManager;
import cn.oyzh.i18n.I18nHelper;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * MariaDB表节点
 *
 * @author oyzh
 * @since 2026-10-09
 */
public class ShellMariadbTableTreeItem extends ShellMariadbTreeItem<ShellMariadbTableTreeItemValue> {

    /**
     * 当前值
     */
    private final MariadbTable value;

    /**
     * 构造表节点
     *
     * @param table    表对象
     * @param treeView 树视图
     */
    public ShellMariadbTableTreeItem(MariadbTable table, RichTreeView treeView) {
        super(treeView);
        this.value = table;
        this.setValue(new ShellMariadbTableTreeItemValue(this));
    }

    @Override
    public ShellMariadbTablesTreeItem parent() {
        return (ShellMariadbTablesTreeItem) super.parent();
    }

    /**
     * 获取db客户端
     *
     * @return db客户端
     */
    public ShellMariadbClient client() {
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
     * 获取表名称
     *
     * @return 表名称
     */
    public String tableName() {
        return this.value.getName();
    }

    /**
     * 获取MariaDB信息
     *
     * @return MariaDB信息
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
        StageManager.showMask(() -> this.doCloneTable(includeRecord));
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
            MariadbTable mariadbTable = this.dbItem().selectTable(cloneTable);
            this.dbItem().getTableTypeChild().addTable(mariadbTable);
        } catch (Exception ex) {
            MessageBox.exception(ex);
        }
    }

    /**
     * 转储
     */
    private void dump() {
        ShellMariadbViewFactory.dumpData(this.client(), this.dbName(), this.tableName(), 2);
    }

    /**
     * 导出
     */
    private void export() {
        ShellMariadbViewFactory.exportData(this.client(), this.dbName(), this.tableName());
    }

    /**
     * 设计表
     */
    private void designTable() {
        this.reloadChild();
        ShellMariadbEventUtil.designTable(this.value, this.dbItem());
    }

    /**
     * 清空表数据并重置自增
     */
    private void truncateTable() {
        if (MessageBox.confirm(I18nHelper.truncateTable() + "[" + this.tableName() + "]")) {
            try {
                this.dbItem().truncateTable(this.tableName());
                ShellMariadbEventUtil.tableTruncated(this, this.dbItem());
            } catch (Exception ex) {
                ex.printStackTrace();
                MessageBox.exception(ex);
            }
        }
    }

    /**
     * 清空表
     */
    private void clearTable() {
        try {
            if (MessageBox.confirm(I18nHelper.clearTable() + "[" + this.tableName() + "]")) {
                this.dbItem().clearTable(this.tableName());
                ShellMariadbEventUtil.tableCleared(this, this.dbItem());
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
                ShellMariadbEventUtil.tableDropped(this, this.dbItem());
                this.parent().clearTableSize();
                this.remove();
            }
        } catch (Exception ex) {
            MessageBox.exception(ex);
        }
    }

    /**
     * 表信息
     */
    private void tableInfo() {
        ShellMariadbViewFactory.tableInfo(this);
    }

    @Override
    public void rename() {
        try {
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
            String oldName = this.value.getName();
            // 修改名称
            this.dbItem().renameTable(oldName, tableName);
            this.value.setName(tableName);
            this.refresh();
            ShellMariadbEventUtil.tableRenamed(oldName, tableName, this.dbItem());
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }

    /**
     * 获取所属数据库节点
     *
     * @return 数据库节点
     */
    public ShellMariadbDatabaseTreeItem dbItem() {
        if (this.parent() == null) {
            return null;
        }
        return this.parent().parent();
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
    public Paging<MariadbRecord> recordPage(long pageNo, long limit, List<MariadbRecordFilter> filters, List<MariadbColumn> columns) {
        MariadbSelectRecordParam param = new MariadbSelectRecordParam();
        param.setLimit(limit);
        param.setFilters(filters);
        param.setColumns(columns);
        param.setDbName(this.dbName());
        param.setStart(pageNo * limit);
        param.setTableName(this.tableName());
        List<MariadbRecord> rows = this.client().selectRecords(param);
        long count = this.client().selectRecordCount(param);
        Paging<MariadbRecord> paging = new Paging<>(rows, limit, count);
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
     * 获取表列列表
     *
     * @return 表列列表
     */
    public List<MariadbColumn> columns() {
        return this.client().selectColumns(new MariadbSelectColumnParam(this.dbName(), this.tableName()));
    }

    /**
     * 获取表索引列表
     *
     * @return 表索引列表
     */
    public List<MariadbIndex> indexes() {
        return this.client().selectIndexes(this.dbName(), this.tableName());
    }

    /**
     * 获取表检查约束列表
     *
     * @return 表检查约束列表
     */
    public List<MariadbCheck> checks() {
        return this.client().selectChecks(this.dbName(), this.tableName());
    }

    /**
     * 获取表外键列表
     *
     * @return 表外键列表
     */
    public List<MariadbForeignKey> foreignKeys() {
        return this.client().selectForeignKeys(this.dbName(), this.tableName());
    }

    /**
     * 获取表触发器列表
     *
     * @return 表触发器列表
     */
    public List<MariadbTrigger> triggers() {
        return this.client().selectTriggers(this.dbName(), this.tableName());
    }

    @Override
    public void onPrimaryDoubleClick() {
        ShellMariadbEventUtil.tableOpen(this, this.dbItem());
    }

    /**
     * 表列信息缓存
     */
    private MariadbColumns columns;

    /**
     * 获取主键列，优先返回自动递增列
     *
     * @return 主键列
     */
    public MariadbColumn getPrimaryKey() {
        if (columns == null) {
            columns = new MariadbColumns(this.columns());
        }
        MariadbColumn dbColumn = null;
        for (MariadbColumn column : this.columns.primaryKeys()) {
            if (column.isAutoIncrement()) {
                dbColumn = column;
                break;
            }
        }
        if (dbColumn == null) {
            for (MariadbColumn column : this.columns.primaryKeys()) {
                return column;
            }
        }
        return dbColumn;
    }

    @Override
    public void loadChild() {
        try {
            MariadbSelectTableParam param = new MariadbSelectTableParam();
            param.setFull(true);
            param.setDbName(this.dbName());
            param.setTableName(this.tableName());
            MariadbTable table = this.client().selectTable(param);
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
     * 是否没有主键
     *
     * @return 结果
     */
    public boolean hasPrimaryKey() {
        if (this.columns == null) {
            this.columns = new MariadbColumns(this.columns());
        }
        return !this.columns.primaryKeys().isEmpty();
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
    public int insertRecord(DBRecordData recordData, MariadbRecordPrimaryKey primaryKey) {
        MariadbInsertRecordParam param = new MariadbInsertRecordParam();
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
        MariadbDeleteRecordParam param = new MariadbDeleteRecordParam();
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
    public int deleteRecord(MariadbRecordPrimaryKey primaryKey) {
        MariadbDeleteRecordParam param = new MariadbDeleteRecordParam();
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
    public MariadbRecord selectRecord(MariadbRecordPrimaryKey primaryKey) {
        MariadbSelectRecordParam param = new MariadbSelectRecordParam();
        param.setDbName(this.dbName());
        param.setTableName(this.tableName());
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
    public int updateRecord(DBRecordData recordData, MariadbRecordPrimaryKey primaryKey) {
        MariadbUpdateRecordParam param = new MariadbUpdateRecordParam();
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
        MariadbUpdateRecordParam param = new MariadbUpdateRecordParam();
        param.setDbName(this.dbName());
        param.setTableName(this.tableName());
        param.setUpdateRecord(recordData);
        param.setRecord(originalRecordData);
        return this.client().updateRecord(param);
    }

    /**
     * 获取表对象
     *
     * @return 表对象
     */
    public MariadbTable value() {
        return value;
    }
}
