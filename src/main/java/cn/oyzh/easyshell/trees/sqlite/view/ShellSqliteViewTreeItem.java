package cn.oyzh.easyshell.trees.sqlite.view;

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
import cn.oyzh.easyshell.sqlite.view.SQLiteView;
import cn.oyzh.easyshell.sqlite.ShellSqliteClient;
import cn.oyzh.easyshell.trees.sqlite.ShellSqliteTreeItem;
import cn.oyzh.easyshell.trees.sqlite.root.ShellSqliteRootTreeItem;
import cn.oyzh.easyshell.util.sqlite.ShellSqliteViewFactory;
import cn.oyzh.fx.db.DBRecordData;
import cn.oyzh.fx.db.util.DBUtil;
import cn.oyzh.fx.gui.menu.MenuItemHelper;
import cn.oyzh.fx.gui.tree.view.RichTreeView;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.menu.FXMenuItem;
import cn.oyzh.fx.plus.window.StageManager;
import cn.oyzh.easyshell.util.ShellI18nHelper;
import cn.oyzh.i18n.I18nHelper;
import javafx.scene.control.MenuItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * SQLite视图节点
 *
 * @author oyzh
 * @since 2026-11-02
 */
public class ShellSqliteViewTreeItem extends ShellSqliteTreeItem<ShellSqliteViewTreeItemValue> {

    /**
     * 当前值
     */
    private final SQLiteView value;

    /**
     * 构造视图节点
     *
     * @param view     视图对象
     * @param treeView 树视图
     */
    public ShellSqliteViewTreeItem(SQLiteView view, RichTreeView treeView) {
        super(treeView);
        super.setFilterable(true);
        this.value = view;
        this.setValue(new ShellSqliteViewTreeItemValue(this));
    }

    @Override
    public ShellSqliteViewsTreeItem parent() {
        return (ShellSqliteViewsTreeItem) super.parent();
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
     * 获取视图对象
     *
     * @return 视图对象
     */
    public SQLiteView value() {
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
     * 获取视图名称
     *
     * @return 视图名称
     */
    public String viewName() {
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
     * 获取视图列信息
     *
     * @return 视图列信息
     */
    public SQLiteColumns viewColumns() {
        this.value.setColumns(new SQLiteColumns(this.client().selectColumns(this.viewName())));
        return this.value.getColumns();
    }

    /**
     * 获取视图列列表
     *
     * @return 视图列列表
     */
    public SQLiteColumns columns() {
        if (this.value.getColumns() == null) {
            return this.viewColumns();
        }
        return this.value.getColumns();
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
     * 视图是否可更新
     *
     * @return 是否可更新
     */
    public boolean isUpdatable() {
        return this.value.isUpdatable();
    }

    /**
     * 分页查询视图记录
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
        param.setReadonly(true);
        param.setFilters(filters);
        param.setColumns(columns);
        param.setDbName(this.dbName());
        param.setStart(pageNo * limit);
        param.setTableName(this.viewName());
        List<SQLiteRecord> records = this.client().selectRecords(param);
        long count = this.client().selectRecordCount(param);
        Paging<SQLiteRecord> paging = new Paging<>(records, limit, count);
        paging.currentPage(pageNo);
        return paging;
    }

    /**
     * 新增视图记录
     *
     * @param recordData 记录数据
     * @return 受影响行数
     */
    public int insertRecord(DBRecordData recordData) {
        return this.insertRecord(recordData, null);
    }

    /**
     * 新增视图记录
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
        param.setTableName(this.viewName());
        return this.client().insertRecord(param);
    }

    /**
     * 删除视图记录
     *
     * @param recordData 记录数据
     * @return 受影响行数
     */
    public int deleteRecord(DBRecordData recordData) {
        SQLiteDeleteRecordParam param = new SQLiteDeleteRecordParam();
        param.setDbName(this.dbName());
        param.setTableName(this.viewName());
        param.setRecord(recordData);
        return this.client().deleteRecord(param);
    }

    /**
     * 删除视图记录
     *
     * @param primaryKey 主键
     * @return 受影响行数
     */
    public int deleteRecord(SQLiteRecordPrimaryKey primaryKey) {
        SQLiteDeleteRecordParam param = new SQLiteDeleteRecordParam();
        param.setDbName(this.dbName());
        param.setTableName(this.viewName());
        param.setPrimaryKey(primaryKey);
        return this.client().deleteRecord(param);
    }

    /**
     * 查询单条视图记录
     *
     * @param primaryKey 主键
     * @return 视图记录
     */
    public SQLiteRecord selectRecord(SQLiteRecordPrimaryKey primaryKey) {
        SQLiteSelectRecordParam param = new SQLiteSelectRecordParam();
        param.setDbName(this.dbName());
        param.setTableName(this.viewName());
        param.setColumns(this.columns());
        param.setPrimaryKey(primaryKey);
        param.setReadonly(true);
        return this.client().selectRecord(param);
    }

    /**
     * 更新视图记录
     *
     * @param recordData 记录数据
     * @param primaryKey 主键
     * @return 受影响行数
     */
    public int updateRecord(DBRecordData recordData, SQLiteRecordPrimaryKey primaryKey) {
        SQLiteUpdateRecordParam param = new SQLiteUpdateRecordParam();
        param.setDbName(this.dbName());
        param.setTableName(this.viewName());
        param.setPrimaryKey(primaryKey);
        param.setUpdateRecord(recordData);
        return this.client().updateRecord(param);
    }

    /**
     * 更新视图记录
     *
     * @param recordData         记录数据
     * @param originalRecordData 原始记录数据
     * @return 受影响行数
     */
    public int updateRecord(DBRecordData recordData, DBRecordData originalRecordData) {
        SQLiteUpdateRecordParam param = new SQLiteUpdateRecordParam();
        param.setDbName(this.dbName());
        param.setTableName(this.viewName());
        param.setUpdateRecord(recordData);
        param.setRecord(originalRecordData);
        return this.client().updateRecord(param);
    }

    @Override
    public void onPrimaryDoubleClick() {
        ShellSqliteEventUtil.viewOpen(this.client(), this.viewName());
    }

    @Override
    public List<MenuItem> getMenuItems() {
        List<MenuItem> items = new ArrayList<>();
        FXMenuItem open = MenuItemHelper.openView(this::onPrimaryDoubleClick);
        FXMenuItem design = MenuItemHelper.designView(this::design);
        FXMenuItem renameView = MenuItemHelper.renameView(this::rename);
        FXMenuItem delete = MenuItemHelper.deleteView(this::delete);
        items.add(open);
        items.add(design);
        items.add(renameView);
        items.add(delete);
        items.add(MenuItemHelper.separator());
        FXMenuItem cloneView = MenuItemHelper.cloneView(this::cloneView);
        FXMenuItem info = MenuItemHelper.viewInfo(this::viewInfo);
        FXMenuItem exportData = MenuItemHelper.exportData(this::exportData);
        items.add(cloneView);
        items.add(info);
        items.add(exportData);
        return items;
    }

    /**
     * 设计视图
     */
    private void design() {
        ShellSqliteEventUtil.designView(this.client(), this.viewName());
    }

    /**
     * 视图信息
     */
    private void viewInfo() {
        ShellSqliteViewFactory.viewInfo(this);
    }

    /**
     * 导出数据
     */
    private void exportData() {
        ShellSqliteViewFactory.exportData(this.client(), this.viewName());
    }

    @Override
    public void rename() {
        String name = MessageBox.prompt(I18nHelper.pleaseInputName(), this.viewName(), this.getWindow());
        // 名称为null或者跟当前名称相同，则忽略
        if (name == null || Objects.equals(name, this.viewName())) {
            return;
        }
        // 检查名称
        if (StringUtil.isBlank(name)) {
            MessageBox.warn(I18nHelper.pleaseInputContent(), this.getWindow());
            return;
        }
        try {
            String oldName = this.viewName();
            // 修改名称
            this.client().renameView(oldName, name);
            this.value.setName(name);
            this.value.setColumns(null);
            this.refresh();
            this.parent().doSort();
            this.parent().clearViewSize();
            ShellSqliteEventUtil.viewChanged(this.client(), oldName, name);
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex, this.getWindow());
        }
    }

    @Override
    public void delete() {
        if (!MessageBox.confirm(ShellI18nHelper.confirm("deleteView", this.viewName()), this.getWindow())) {
            return;
        }
        try {
            this.client().dropView(this.viewName());
            this.parent().clearViewSize();
            ShellSqliteEventUtil.viewChanged(this.client(), this.viewName(), null);
            this.remove();
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex, this.getWindow());
        }
    }

    /**
     * 克隆视图
     */
    private void cloneView() {
        StageManager.showMask(this.getWindow(), () -> {
            try {
                String cloneName = this.viewName() + DBUtil.genCloneName();
                this.client().cloneView(this.viewName(), cloneName);
                ShellSqliteEventUtil.viewChanged(this.client(), null, cloneName);
            } catch (Exception ex) {
                ex.printStackTrace();
                MessageBox.exception(ex, this.getWindow());
            }
        });
    }
}
