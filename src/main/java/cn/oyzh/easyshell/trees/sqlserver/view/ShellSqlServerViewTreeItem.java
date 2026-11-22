package cn.oyzh.easyshell.trees.sqlserver.view;

import cn.oyzh.common.dto.Paging;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.sqlserver.ShellSqlServerClient;
import cn.oyzh.easyshell.sqlserver.column.SqlServerColumn;
import cn.oyzh.easyshell.sqlserver.column.SqlServerColumns;
import cn.oyzh.easyshell.sqlserver.record.SqlServerDeleteRecordParam;
import cn.oyzh.easyshell.sqlserver.record.SqlServerInsertRecordParam;
import cn.oyzh.easyshell.sqlserver.record.SqlServerRecord;
import cn.oyzh.easyshell.sqlserver.record.SqlServerRecordFilter;
import cn.oyzh.easyshell.sqlserver.record.SqlServerRecordPrimaryKey;
import cn.oyzh.easyshell.sqlserver.record.SqlServerSelectRecordParam;
import cn.oyzh.easyshell.sqlserver.record.SqlServerUpdateRecordParam;
import cn.oyzh.easyshell.sqlserver.view.SqlServerView;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.event.sqlserver.ShellSqlServerEventUtil;
import cn.oyzh.easyshell.trees.sqlserver.ShellSqlServerTreeItem;
import cn.oyzh.easyshell.trees.sqlserver.schema.ShellSqlServerSchemaTreeItem;
import cn.oyzh.easyshell.util.sqlserver.ShellSqlServerViewFactory;
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
 * SqlServer数据库树视图节点
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class ShellSqlServerViewTreeItem extends ShellSqlServerTreeItem<ShellSqlServerViewTreeItemValue> {

    /**
     * 当前值
     */
    private final SqlServerView value;

    /**
     * 获取视图值
     *
     * @return 视图值
     */
    public SqlServerView value() {
        return value;
    }

    /**
     * 构造SqlServer数据库树视图节点
     *
     * @param view     视图
     * @param treeView 树视图
     */
    public ShellSqlServerViewTreeItem(SqlServerView view, RichTreeView treeView) {
        super(treeView);
        super.setFilterable(true);
        this.value = view;
        this.setValue(new ShellSqlServerViewTreeItemValue(this));
    }

    @Override
    public ShellSqlServerViewsTreeItem parent() {
        return (ShellSqlServerViewsTreeItem) super.parent();
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
     * 获取连接信息
     *
     * @return 连接信息
     */
    public ShellConnect info() {
        return this.parent().info();
    }

    /**
     * 获取视图列
     *
     * @return 视图列
     */
    public SqlServerColumns viewColumns() {
        this.value.setColumns(new SqlServerColumns(this.columns()));
        return this.value.getColumns();
    }

    @Override
    public List<MenuItem> getMenuItems() {
        List<MenuItem> items = new ArrayList<>();
        FXMenuItem open = MenuItemHelper.openView(this::onPrimaryDoubleClick);
        items.add(open);
        FXMenuItem design = MenuItemHelper.designView(this::designView);
        items.add(design);
        FXMenuItem renameView = MenuItemHelper.renameView(this::rename);
        items.add(renameView);
        FXMenuItem delete = MenuItemHelper.deleteView(this::delete);
        items.add(delete);
        items.add(MenuItemHelper.separator());
        FXMenuItem cloneView = MenuItemHelper.cloneView(this::cloneView);
        items.add(cloneView);
        FXMenuItem info = MenuItemHelper.viewInfo(this::viewInfo);
        items.add(info);
        return items;
    }

    /**
     * 克隆视图
     */
    private void cloneView() {
        StageManager.showMask(this.getWindow(), this::doCloneView);
    }

    /**
     * 执行克隆视图
     */
    private void doCloneView() {
        try {
            String cloneView = this.viewName() + DBUtil.genCloneName();
            this.dbItem().cloneView(this.viewName(), cloneView);
            SqlServerView view = this.dbItem().selectView(cloneView);
            this.dbItem().getViewTypeChild().addView(view);
        } catch (Exception ex) {
            MessageBox.exception(ex, this.getWindow());
        }
    }

    /**
     * 视图信息
     */
    private void viewInfo() {
        ShellSqlServerViewFactory.viewInfo(this);
    }

    /**
     * 设计视图
     */
    private void designView() {
        ShellSqlServerEventUtil.designView(this.value, this.dbItem());
    }

    @Override
    public void delete() {
        if (!MessageBox.confirm(ShellI18nHelper.confirmDeleteView(this.value.getName()), this.getWindow())) {
            return;
        }
        try {
            this.dbItem().dropView(this.value);
            ShellSqlServerEventUtil.dropView(this);
            this.parent().clearViewSize();
            super.remove();
        } catch (Exception ex) {
            MessageBox.exception(ex, this.getWindow());
        }
    }

    /**
     * 获取所属模式节点
     *
     * @return 模式节点
     */
    public ShellSqlServerSchemaTreeItem dbItem() {
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
        param.setStart(pageNo * limit);
        param.setSchema(this.schema());
        param.setTableName(this.viewName());
        List<SqlServerRecord> records = this.client().viewRecords(this.schema(), this.viewName(), pageNo * limit, limit, filters);
        long count = this.client().selectRecordCount(param);
        Paging<SqlServerRecord> paging = new Paging<>(records, limit, count);
        paging.currentPage(pageNo);
        return paging;
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
     * 查询列
     *
     * @return 列
     */
    public SqlServerColumns columns() {
        return new SqlServerColumns(this.client().viewColumns(this.schema(), this.viewName()));
    }

    @Override
    public void onPrimaryDoubleClick() {
        ShellSqlServerEventUtil.viewOpen(this, this.dbItem());
    }

    /**
     * 获取主键列，优先返回自动递增列
     *
     * @return 主键列
     */
    public SqlServerColumn getPrimaryKey() {
        if (this.value.getColumns() == null) {
            this.viewColumns();
        }
        SqlServerColumn dbColumn = null;
        if (this.value.columns() != null) {
            for (SqlServerColumn column : this.value.columns()) {
                if (column.isAutoIncrement()) {
                    dbColumn = column;
                    break;
                }
            }
        }
        return dbColumn;
    }

    /**
     * 判断视图是否可更新
     *
     * @return 是否可更新
     */
    public boolean isUpdatable() {
        return this.value.isUpdatable();
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
        param.setTableName(this.viewName());
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
        param.setRecord(recordData);
        param.setSchema(this.schema());
        param.setTableName(this.viewName());
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
        param.setTableName(this.viewName());
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
        param.setTableName(this.viewName());
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
        param.setTableName(this.viewName());
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
        param.setTableName(this.viewName());
        param.setUpdateRecord(recordData);
        param.setRecord(originalRecordData);
        return this.client().updateRecord(param);
    }

    @Override
    public void rename() {
        try {
            String newName = MessageBox.prompt(I18nHelper.pleaseInputName(), this.viewName(), this.getWindow());
            // 名称为null或者跟当前名称相同，则忽略
            if (newName == null || Objects.equals(newName, this.viewName())) {
                return;
            }
            // 检查名称
            if (StringUtil.isBlank(newName)) {
                MessageBox.warn(I18nHelper.pleaseInputContent(), this.getWindow());
                return;
            }
            String oldName = this.viewName();
            // 修改名称
            this.dbItem().renameView(oldName, newName);
            this.value.setName(newName);
            this.refresh();
            ShellSqlServerEventUtil.viewRenamed(oldName, newName, this.dbItem());
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex, this.getWindow());
        }
    }
}
