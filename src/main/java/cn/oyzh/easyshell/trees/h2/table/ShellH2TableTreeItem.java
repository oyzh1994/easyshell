package cn.oyzh.easyshell.trees.h2.table;

import cn.oyzh.common.dto.Paging;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.event.h2.ShellH2EventUtil;
import cn.oyzh.easyshell.h2.column.H2Column;
import cn.oyzh.easyshell.h2.column.H2Columns;
import cn.oyzh.easyshell.h2.column.H2SelectColumnParam;
import cn.oyzh.easyshell.h2.record.H2DeleteRecordParam;
import cn.oyzh.easyshell.h2.record.H2InsertRecordParam;
import cn.oyzh.easyshell.h2.record.H2Record;
import cn.oyzh.easyshell.h2.record.H2RecordFilter;
import cn.oyzh.easyshell.h2.record.H2RecordPrimaryKey;
import cn.oyzh.easyshell.h2.record.H2SelectRecordParam;
import cn.oyzh.easyshell.h2.record.H2UpdateRecordParam;
import cn.oyzh.easyshell.h2.table.H2Table;
import cn.oyzh.easyshell.h2.ShellH2Client;
import cn.oyzh.easyshell.trees.h2.ShellH2TreeItem;
import cn.oyzh.easyshell.trees.h2.root.ShellH2RootTreeItem;
import cn.oyzh.easyshell.util.h2.ShellH2ViewFactory;
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
 * H2表节点
 *
 * @author oyzh
 * @since 2026-11-02
 */
public class ShellH2TableTreeItem extends ShellH2TreeItem<ShellH2TableTreeItemValue> {

    /**
     * 当前值
     */
    private final H2Table value;

    /**
     * 最近一次加载的字段列表
     */
    private H2Columns columns;

    /**
     * 构造表节点
     *
     * @param table    表对象
     * @param treeView 树视图
     */
    public ShellH2TableTreeItem(H2Table table, RichTreeView treeView) {
        super(treeView);
        this.value = table;
        this.setValue(new ShellH2TableTreeItemValue(this));
    }

    @Override
    public ShellH2TablesTreeItem parent() {
        return (ShellH2TablesTreeItem) super.parent();
    }

    /**
     * 获取数据库树节点
     *
     * @return 数据库树节点
     */
    public ShellH2RootTreeItem dbItem() {
        return this.parent().parent().parent();
    }

    /**
     * 获取表对象
     *
     * @return 表对象
     */
    public H2Table value() {
        return this.value;
    }

    /**
     * 获取H2客户端
     *
     * @return H2客户端
     */
    public ShellH2Client client() {
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
    public H2Columns columns() {
        // 表结构可能被设计器、终端或 SQL 修改，字段列表必须实时读取，避免记录页继续显示旧字段
        this.columns = new H2Columns(this.client().selectColumns(
                new H2SelectColumnParam(this.dbName(), this.tableName())));
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
    public H2Column getPrimaryKey() {
        List<H2Column> primaryKeys = this.columns().primaryKeys();
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
    public Paging<H2Record> recordPage(long pageNo, long limit, List<H2RecordFilter> filters,
                                           List<H2Column> columns) {
        H2SelectRecordParam param = new H2SelectRecordParam();
        param.setLimit(limit);
        param.setStart(pageNo * limit);
        param.setFilters(filters);
        param.setColumns(columns);
        param.setDbName(this.dbName());
        param.setTableName(this.tableName());
        List<H2Record> records = this.client().selectRecords(param);
        long count = this.client().selectRecordCount(param);
        Paging<H2Record> paging = new Paging<>(records, limit, count);
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
    public int insertRecord(DBRecordData recordData, H2RecordPrimaryKey primaryKey) {
        H2InsertRecordParam param = new H2InsertRecordParam();
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
        H2DeleteRecordParam param = new H2DeleteRecordParam();
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
    public int deleteRecord(H2RecordPrimaryKey primaryKey) {
        H2DeleteRecordParam param = new H2DeleteRecordParam();
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
    public H2Record selectRecord(H2RecordPrimaryKey primaryKey) {
        H2SelectRecordParam param = new H2SelectRecordParam();
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
    public int updateRecord(DBRecordData recordData, H2RecordPrimaryKey primaryKey) {
        H2UpdateRecordParam param = new H2UpdateRecordParam();
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
        H2UpdateRecordParam param = new H2UpdateRecordParam();
        param.setDbName(this.dbName());
        param.setTableName(this.tableName());
        param.setUpdateRecord(recordData);
        param.setRecord(originalRecordData);
        return this.client().updateRecord(param);
    }

    @Override
    public void onPrimaryDoubleClick() {
        ShellH2EventUtil.tableOpen(this.client(), this.dbName(), this.tableName());
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
        ShellH2EventUtil.designTable(this.client(), this.dbName(), this.tableName());
    }

    /**
     * 表信息
     */
    private void tableInfo() {
        ShellH2ViewFactory.tableInfo(this);
    }

    /**
     * 导出数据
     */
    private void exportData() {
        ShellH2ViewFactory.exportData(this.client(), this.tableName());
    }

    /**
     * 转储数据
     */
    private void dumpData() {
        ShellH2ViewFactory.dumpData(this.client(), this.dbName(), this.tableName(), 2, 0);
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
            this.client().renameTable(this.dbName(), oldName, name);
            this.value.setName(name);
            this.clearColumns();
            this.refresh();
            this.parent().doSort();
//            this.parent().clearTableSize();
            ShellH2EventUtil.tableChanged(this.client(), this.dbName(), oldName, name);
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex, this.getWindow());
        }
    }

    @Override
    public void delete() {
        if (!MessageBox.confirm(ShellI18nHelper.confirmDeleteTable(this.tableName()), this.getWindow())) {
            return;
        }
        try {
            this.client().dropTable(this.dbName(), this.tableName());
//            this.parent().clearTableSize();
            ShellH2EventUtil.tableChanged(this.client(), this.dbName(), this.tableName(), null);
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
        if (!MessageBox.confirm(ShellI18nHelper.confirmClearTable(this.tableName()), this.getWindow())) {
            return;
        }
        try {
            this.client().clearTable(this.dbName(), this.tableName());
            ShellH2EventUtil.tableChanged(this.client(), this.dbName(),
                    this.tableName(), this.tableName());
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex, this.getWindow());
        }
    }

    /**
     * 清空表并重置自增
     */
    private void truncateTable() {
        if (!MessageBox.confirm(ShellI18nHelper.confirmTruncateTable(this.tableName()), this.getWindow())) {
            return;
        }
        try {
            this.client().truncateTable(this.dbName(), this.tableName());
            ShellH2EventUtil.tableChanged(this.client(), this.dbName(),
                    this.tableName(), this.tableName());
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
                ShellH2EventUtil.tableChanged(this.client(), this.dbName(), null, cloneName);
            } catch (Exception ex) {
                ex.printStackTrace();
                MessageBox.exception(ex, this.getWindow());
            }
        });
    }
}
