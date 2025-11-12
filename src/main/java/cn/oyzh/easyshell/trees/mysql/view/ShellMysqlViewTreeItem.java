package cn.oyzh.easyshell.trees.mysql.view;

import cn.oyzh.common.dto.Paging;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.event.mysql.ShellMysqlEventUtil;
import cn.oyzh.easyshell.mysql.ShellMysqlClient;
import cn.oyzh.easyshell.mysql.column.MysqlColumn;
import cn.oyzh.easyshell.mysql.column.MysqlColumns;
import cn.oyzh.easyshell.mysql.record.MysqlDeleteRecordParam;
import cn.oyzh.easyshell.mysql.record.MysqlInsertRecordParam;
import cn.oyzh.easyshell.mysql.record.MysqlRecord;
import cn.oyzh.easyshell.mysql.record.MysqlRecordData;
import cn.oyzh.easyshell.mysql.record.MysqlRecordFilter;
import cn.oyzh.easyshell.mysql.record.MysqlRecordPrimaryKey;
import cn.oyzh.easyshell.mysql.record.MysqlSelectRecordParam;
import cn.oyzh.easyshell.mysql.record.MysqlUpdateRecordParam;
import cn.oyzh.easyshell.mysql.view.MysqlView;
import cn.oyzh.easyshell.trees.mysql.ShellMysqlTreeItem;
import cn.oyzh.easyshell.trees.mysql.database.ShellMysqlDatabaseTreeItem;
import cn.oyzh.easyshell.util.mysql.ShellMysqlUtil;
import cn.oyzh.fx.gui.menu.MenuItemHelper;
import cn.oyzh.fx.gui.tree.view.RichTreeView;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.menu.FXMenuItem;
import cn.oyzh.fx.plus.window.StageManager;
import cn.oyzh.i18n.I18nHelper;
import javafx.scene.control.MenuItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * db树视图节点
 *
 * @author oyzh
 * @since 2024/12/27
 */
public class ShellMysqlViewTreeItem extends ShellMysqlTreeItem<ShellMysqlViewTreeItemValue> {

    /**
     * 当前值
     */
    private final MysqlView value;

    public MysqlView value() {
        return value;
    }

    public ShellMysqlViewTreeItem(MysqlView view, RichTreeView treeView) {
        super(treeView);
        super.setFilterable(true);
        this.value = view;
        this.setValue(new ShellMysqlViewTreeItemValue(this));
    }

    @Override
    public ShellMysqlViewsTreeItem parent() {
        return (ShellMysqlViewsTreeItem) super.parent();
    }

    public ShellMysqlClient client() {
        return this.parent().client();
    }

    public String dbName() {
        return this.parent().dbName();
    }

    /**
     * 获取redis信息
     *
     * @return redis信息
     */
    public ShellConnect info() {
        return this.parent().info();
    }

    public MysqlColumns viewColumns() {
        this.value.setColumns(new MysqlColumns(this.columns()));
        return this.value.getColumns();
    }

    @Override
    public List<MenuItem> getMenuItems() {
        List<MenuItem> items = new ArrayList<>();
        FXMenuItem open = MenuItemHelper.openView("12", this::onPrimaryDoubleClick);
        items.add(open);
        FXMenuItem design = MenuItemHelper.designView("12", this::designView);
        items.add(design);
        FXMenuItem renameView = MenuItemHelper.renameView("12", this::rename);
        items.add(renameView);
        // FXMenuItem info = MenuItemHelper.viewInfo("12", this::viewInfo);
        // items.add(info);
        FXMenuItem delete = MenuItemHelper.deleteView("12", this::delete);
        items.add(delete);
        items.add(MenuItemHelper.separator());
        FXMenuItem cloneView = MenuItemHelper.cloneView("12", this::cloneView);
        items.add(cloneView);
        return items;
    }

    /**
     * 克隆视图
     */
    private void cloneView() {
        StageManager.showMask(this::doCloneView);
    }

    /**
     * 执行克隆视图
     */
    private void doCloneView() {
        try {
            String cloneView = this.viewName() + ShellMysqlUtil.genCloneName();
            this.dbItem().cloneView(this.viewName(), cloneView);
            MysqlView mysqlView = this.dbItem().selectView(cloneView);
            this.dbItem().getViewTypeChild().addView(mysqlView);
        } catch (Exception ex) {
            MessageBox.exception(ex);
        }
    }

    // private void viewInfo() {
    //     StageAdapter fxView = StageManager.parseStage(MysqlViewInfoController.class, this.window());
    //     fxView.setProp("item", this);
    //     fxView.display();
    // }

    private void designView() {
        ShellMysqlEventUtil.designView(this.value, this.dbItem());
    }

    @Override
    public void delete() {
        if (!MessageBox.confirm(I18nHelper.deleteView() + " " + this.value.getName() + "?")) {
            return;
        }
        try {
            this.dbItem().dropView(this.value);
            ShellMysqlEventUtil.dropView(this);
            super.remove();
        } catch (Exception ex) {
            MessageBox.exception(ex);
        }
    }

    public ShellMysqlDatabaseTreeItem dbItem() {
        return this.parent().parent();
    }

    public Paging<MysqlRecord> recordPage(long pageNo, long limit, List<MysqlRecordFilter> filters, List<MysqlColumn> columns) {
        MysqlSelectRecordParam param = new MysqlSelectRecordParam();
        param.setLimit(limit);
        param.setFilters(filters);
        param.setColumns(columns);
        param.setStart(pageNo * limit);
        param.setDbName(this.dbName());
        param.setTableName(this.viewName());
        List<MysqlRecord> records = this.client().viewRecords(this.dbName(), this.viewName(), pageNo * limit, limit, filters);
        long count = this.client().selectRecordCount(param);
        Paging<MysqlRecord> paging = new Paging<>(records, limit, count);
        paging.currentPage(pageNo);
        return paging;
    }

    public String infoName() {
        return this.parent().infoName();
    }

    public MysqlColumns columns() {
        return new MysqlColumns(this.client().viewColumns(this.dbName(), this.viewName()));
    }

    @Override
    public void onPrimaryDoubleClick() {
        ShellMysqlEventUtil.viewOpen(this, this.dbItem());
    }

    /**
     * 获取主键列，优先返回自动递增列
     *
     * @return 主键列
     */
    public MysqlColumn getPrimaryKey() {
        if (this.value.getColumns() == null) {
            this.viewColumns();
        }
        MysqlColumn dbColumn = null;
        if (this.value.columns() != null) {
            for (MysqlColumn column : this.value.columns()) {
                if (column.isAutoIncrement()) {
                    dbColumn = column;
                    break;
                }
            }
        }
        return dbColumn;
    }

    public boolean isUpdatable() {
        return this.value.isUpdatable();
    }

    public String viewName() {
        return this.value.getName();
    }

    public int insertRecord(MysqlRecordData recordData) {
        return this.insertRecord(recordData, null);
    }

    public int insertRecord(MysqlRecordData recordData, MysqlRecordPrimaryKey primaryKey) {
        MysqlInsertRecordParam param = new MysqlInsertRecordParam();
        param.setRecord(recordData);
        param.setDbName(this.dbName());
        param.setPrimaryKey(primaryKey);
        param.setTableName(this.viewName());
        return this.client().insertRecord(param);
    }

    public int deleteRecord(MysqlRecordData recordData) {
        MysqlDeleteRecordParam param = new MysqlDeleteRecordParam();
        param.setRecord(recordData);
        param.setDbName(this.dbName());
        param.setTableName(this.viewName());
        return this.client().deleteRecord(param);
    }

    public int deleteRecord(MysqlRecordPrimaryKey primaryKey) {
        MysqlDeleteRecordParam param = new MysqlDeleteRecordParam();
        param.setDbName(this.dbName());
        param.setTableName(this.viewName());
        param.setPrimaryKey(primaryKey);
        return this.client().deleteRecord(param);
    }

    public MysqlRecord selectRecord(MysqlRecordPrimaryKey primaryKey) {
        MysqlSelectRecordParam param = new MysqlSelectRecordParam();
        param.setDbName(this.dbName());
        param.setTableName(this.viewName());
        param.setPrimaryKey(primaryKey);
        return this.client().selectRecord(param);
    }

    public int updateRecord(MysqlRecordData recordData, MysqlRecordPrimaryKey primaryKey) {
        MysqlUpdateRecordParam param = new MysqlUpdateRecordParam();
        param.setDbName(this.dbName());
        param.setTableName(this.viewName());
        param.setPrimaryKey(primaryKey);
        param.setUpdateRecord(recordData);
        return this.client().updateRecord(param);
    }

    public int updateRecord(MysqlRecordData recordData, MysqlRecordData originalRecordData) {
        MysqlUpdateRecordParam param = new MysqlUpdateRecordParam();
        param.setDbName(this.dbName());
        param.setTableName(this.viewName());
        param.setUpdateRecord(recordData);
        param.setRecord(originalRecordData);
        return this.client().updateRecord(param);
    }

    @Override
    public void rename() {
        try {
            String viewName = MessageBox.prompt(I18nHelper.pleaseInputName(), this.viewName());
            // 名称为null或者跟当前名称相同，则忽略
            if (viewName == null || Objects.equals(viewName, this.viewName())) {
                return;
            }
            // 检查名称
            if (StringUtil.isBlank(viewName)) {
                MessageBox.warn(I18nHelper.pleaseInputContent());
                return;
            }
            String oldName = this.viewName();
            // 修改名称
            this.dbItem().renameTable(oldName, viewName);
            this.value.setName(viewName);
            this.refresh();
            ShellMysqlEventUtil.viewRenamed(this, this.dbItem());
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }
}
