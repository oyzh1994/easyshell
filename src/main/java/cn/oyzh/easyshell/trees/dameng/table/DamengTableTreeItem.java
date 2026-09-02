package cn.oyzh.easyshell.trees.dameng.table;

import cn.oyzh.common.dto.Paging;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.controller.dameng.data.ShellDamengDataDumpController;
import cn.oyzh.easyshell.controller.dameng.data.ShellDamengDataExportController;
import cn.oyzh.easyshell.controller.dameng.table.ShellDamengTableInfoController;
import cn.oyzh.easyshell.dameng.ShellDamengClient;
import cn.oyzh.easyshell.dameng.check.DamengChecks;
import cn.oyzh.easyshell.dameng.column.DamengColumn;
import cn.oyzh.easyshell.dameng.column.DamengColumns;
import cn.oyzh.easyshell.dameng.column.DamengSelectColumnParam;
import cn.oyzh.easyshell.dameng.foreignKey.DamengForeignKey;
import cn.oyzh.easyshell.dameng.index.DamengIndex;
import cn.oyzh.easyshell.dameng.record.DamengDeleteRecordParam;
import cn.oyzh.easyshell.dameng.record.DamengInsertRecordParam;
import cn.oyzh.easyshell.dameng.record.DamengRecord;
import cn.oyzh.easyshell.dameng.record.DamengRecordData;
import cn.oyzh.easyshell.dameng.record.DamengRecordFilter;
import cn.oyzh.easyshell.dameng.record.DamengRecordPrimaryKey;
import cn.oyzh.easyshell.dameng.record.DamengSelectRecordParam;
import cn.oyzh.easyshell.dameng.record.DamengUpdateRecordParam;
import cn.oyzh.easyshell.dameng.table.DamengTable;
import cn.oyzh.easyshell.dameng.trigger.DamengTrigger;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.event.dameng.DamengEventUtil;
import cn.oyzh.easyshell.trees.dameng.DBTreeItem;
import cn.oyzh.easyshell.trees.dameng.schema.DamengSchemaTreeItem;
import cn.oyzh.easyshell.util.dameng.DamengI18nHelper;
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
public class DamengTableTreeItem extends DBTreeItem<DamengTableTreeItemValue> {

    /**
     * 当前值
     */
    private final DamengTable value;

    public DamengTableTreeItem(DamengTable table, RichTreeView treeView) {
        super(treeView);
        this.value = table;
        this.setValue(new DamengTableTreeItemValue(this));
    }

    @Override
    public DamengTablesTreeItem parent() {
        return (DamengTablesTreeItem) super.parent();
    }

    public ShellDamengClient client() {
        return this.parent().client();
    }

    public String schema() {
        return this.parent().schema();
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
        FXMenuItem openTable = MenuItemHelper.openTable(this::onPrimaryDoubleClick);
        items.add(openTable);
        FXMenuItem updateTable = MenuItemHelper.designTable(this::designTable);
        items.add(updateTable);
        FXMenuItem renameTable = MenuItemHelper.renameTable(this::rename);
        items.add(renameTable);
        FXMenuItem clearTable = MenuItemHelper.clearTableData(this::clearTableData);
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
        FXMenuItem tableInfo = MenuItemHelper.tableInfo(this::tableInfo);
        items.add(tableInfo);

        // 克隆表
        Menu cloneTable = MenuItemHelper.menu(I18nHelper.cloneTable(), new CopySVGGlyph());
        MenuItem clone1 = MenuItemHelper.menuItem(DamengI18nHelper.tableTip3(), () -> this.cloneTable(true));
        MenuItem clone2 = MenuItemHelper.menuItem(DamengI18nHelper.tableTip4(), () -> this.cloneTable(false));
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
            DamengTable damengTable = this.dbItem().selectTable(cloneTable);
            this.dbItem().getTableTypeChild().addTable(damengTable);
        } catch (Exception ex) {
            MessageBox.exception(ex);
        }
    }

    /**
     * 转储
     */
    private void dump() {
        StageAdapter fxView = StageManager.parseStage(ShellDamengDataDumpController.class, this.window());
        fxView.setProp("dumpType", 2);
        fxView.setProp("dbInfo", this.info());
        fxView.setProp("dbName", this.schema());
        fxView.setProp("dbClient", this.client());
        fxView.setProp("tableName", this.tableName());
        fxView.display();
    }

    /**
     * 导出
     */
    private void export() {
        StageAdapter fxView = StageManager.parseStage(ShellDamengDataExportController.class, this.window());
        fxView.setProp("dumpType", 2);
        fxView.setProp("dbInfo", this.info());
        fxView.setProp("dbName", this.schema());
        fxView.setProp("dbClient", this.client());
        fxView.setProp("tableName", this.tableName());
        fxView.display();
    }

    private void designTable() {
        this.reloadChild();
        DamengEventUtil.designTable(this.value, this.dbItem());
    }

    private void truncateTable() {
        if (MessageBox.confirm(I18nHelper.truncateTable() + "[" + this.tableName() + "]")) {
            try {
                this.dbItem().truncateTable(this.tableName());
                DamengEventUtil.tableTruncated(this, this.dbItem());
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
                DamengEventUtil.tableCleared(this, this.dbItem());
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
                DamengEventUtil.tableDropped(this, this.dbItem());
                this.remove();
            }
        } catch (Exception ex) {
            MessageBox.exception(ex);
        }
    }

    private void tableInfo() {
        StageAdapter fxView = StageManager.parseStage(ShellDamengTableInfoController.class, this.window());
        fxView.setProp("item", this);
        fxView.display();
    }

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
            DamengEventUtil.tableRenamed(this, this.dbItem());
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }

    public DamengSchemaTreeItem dbItem() {
        if (this.parent() == null) {
            return null;
        }
        return this.parent().parent();
    }

    public Paging<DamengRecord> recordPage(long pageNo, long limit, List<DamengRecordFilter> filters, List<DamengColumn> columns) {
        DamengSelectRecordParam param = new DamengSelectRecordParam();
        param.setLimit(limit);
        param.setFilters(filters);
        param.setColumns(columns);
        param.setSchema(this.schema());
        param.setStart(pageNo * limit);
        param.setTableName(this.tableName());
        List<DamengRecord> rows = this.client().selectRecords(param);
        long count = this.client().selectRecordCount(param);
        Paging<DamengRecord> paging = new Paging<>(rows, limit, count);
        paging.currentPage(pageNo);
        return paging;
    }

    public String infoName() {
        return parent().infoName();
    }

    public DamengColumns columns() {
        return this.client().selectColumns(new DamengSelectColumnParam(this.schema(), this.tableName()));
    }

    public List<DamengIndex> indexes() {
        return this.client().indexes(this.schema(), this.tableName());
    }

    public DamengChecks checks() {
        return this.client().checks(this.schema(), this.tableName());
    }

    public List<DamengForeignKey> foreignKeys() {
        return this.client().foreignKeys(this.schema(), this.tableName());
    }

    public List<DamengTrigger> triggers() {
        return this.client().selectTriggers(this.schema(), this.tableName());
    }

    @Override
    public void onPrimaryDoubleClick() {
        DamengEventUtil.tableOpen(this, this.dbItem());
    }

    private DamengColumns columns;

    /**
     * 获取主键列，优先返回自动递增列
     *
     * @return 主键列
     */
    public DamengColumn getPrimaryKey() {
        if (columns == null) {
            columns = this.columns();
        }
        DamengColumn dbColumn = null;
        for (DamengColumn column : this.columns.primaryKeys()) {
            if (column.isAutoIncrement()) {
                dbColumn = column;
                break;
            }
        }
        if (dbColumn == null) {
            for (DamengColumn column : this.columns.primaryKeys()) {
                return column;
            }
        }
        return dbColumn;
    }

    @Override
    public void loadChild() {
        try {
            DamengTable table = this.client().selectTable(this.schema(), this.tableName());
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

    public int insertRecord(DamengRecordData recordData) {
        return this.insertRecord(recordData, null);
    }

    public int insertRecord(DamengRecordData recordData, DamengRecordPrimaryKey primaryKey) {
        DamengInsertRecordParam param = new DamengInsertRecordParam();
        param.setRecord(recordData);
        param.setSchema(this.schema());
        param.setPrimaryKey(primaryKey);
        param.setTableName(this.tableName());
        return this.client().insertRecord(param);
    }

    public int deleteRecord(DamengRecordData recordData) {
        DamengDeleteRecordParam param = new DamengDeleteRecordParam();
        param.setSchema(this.schema());
        param.setTableName(this.tableName());
        param.setRecord(recordData);
        return this.client().deleteRecord(param);
    }

    public int deleteRecord(DamengRecordPrimaryKey primaryKey) {
        DamengDeleteRecordParam param = new DamengDeleteRecordParam();
        param.setSchema(this.schema());
        param.setTableName(this.tableName());
        param.setPrimaryKey(primaryKey);
        return this.client().deleteRecord(param);
    }

    public DamengRecord selectRecord(DamengRecordPrimaryKey primaryKey) {
        DamengSelectRecordParam param = new DamengSelectRecordParam();
        param.setSchema(this.schema());
        param.setTableName(this.tableName());
        param.setPrimaryKey(primaryKey);
        return this.client().selectRecord(param);
    }

    public int updateRecord(DamengRecordData recordData, DamengRecordPrimaryKey primaryKey) {
        DamengUpdateRecordParam param = new DamengUpdateRecordParam();
        param.setSchema(this.schema());
        param.setTableName(this.tableName());
        param.setPrimaryKey(primaryKey);
        param.setUpdateRecord(recordData);
        return this.client().updateRecord(param);
    }

    public int updateRecord(DamengRecordData recordData, DamengRecordData originalRecordData) {
        DamengUpdateRecordParam param = new DamengUpdateRecordParam();
        param.setSchema(this.schema());
        param.setTableName(this.tableName());
        param.setUpdateRecord(recordData);
        param.setRecord(originalRecordData);
        return this.client().updateRecord(param);
    }

    public DamengTable value() {
        return value;
    }
}
