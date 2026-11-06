package cn.oyzh.easyshell.trees.postgresql.view;

import cn.oyzh.common.dto.Paging;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.postgresql.ShellPostgresqlClient;
import cn.oyzh.easyshell.postgresql.column.PostgresqlColumn;
import cn.oyzh.easyshell.postgresql.column.PostgresqlColumns;
import cn.oyzh.easyshell.postgresql.record.PostgresqlDeleteRecordParam;
import cn.oyzh.easyshell.postgresql.record.PostgresqlInsertRecordParam;
import cn.oyzh.easyshell.postgresql.record.PostgresqlRecord;
import cn.oyzh.easyshell.postgresql.record.PostgresqlRecordFilter;
import cn.oyzh.easyshell.postgresql.record.PostgresqlRecordPrimaryKey;
import cn.oyzh.easyshell.postgresql.record.PostgresqlSelectRecordParam;
import cn.oyzh.easyshell.postgresql.record.PostgresqlUpdateRecordParam;
import cn.oyzh.easyshell.postgresql.view.PostgresqlView;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.event.postgresql.ShellPostgresqlEventUtil;
import cn.oyzh.easyshell.trees.postgresql.ShellPostgresqlTreeItem;
import cn.oyzh.easyshell.trees.postgresql.schema.ShellPostgresqlSchemaTreeItem;
import cn.oyzh.easyshell.util.postgresql.ShellPostgresqlViewFactory;
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
 * PostgreSQL数据库树视图节点
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class ShellPostgresqlViewTreeItem extends ShellPostgresqlTreeItem<ShellPostgresqlViewTreeItemValue> {

    /**
     * 当前值
     */
    private final PostgresqlView value;

    /**
     * 获取视图值
     *
     * @return 视图值
     */
    public PostgresqlView value() {
        return value;
    }

    /**
     * 构造PostgreSQL数据库树视图节点
     *
     * @param view     视图
     * @param treeView 树视图
     */
    public ShellPostgresqlViewTreeItem(PostgresqlView view, RichTreeView treeView) {
        super(treeView);
        super.setFilterable(true);
        this.value = view;
        this.setValue(new ShellPostgresqlViewTreeItemValue(this));
    }

    @Override
    public ShellPostgresqlViewsTreeItem parent() {
        return (ShellPostgresqlViewsTreeItem) super.parent();
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
    public PostgresqlColumns viewColumns() {
        this.value.setColumns(new PostgresqlColumns(this.columns()));
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
            PostgresqlView view = this.dbItem().selectView(cloneView);
            this.dbItem().getViewTypeChild().addView(view);
        } catch (Exception ex) {
            MessageBox.exception(ex, this.getWindow());
        }
    }

    /**
     * 视图信息
     */
    private void viewInfo() {
        ShellPostgresqlViewFactory.viewInfo(this);
    }

    /**
     * 设计视图
     */
    private void designView() {
        ShellPostgresqlEventUtil.designView(this.value, this.dbItem());
    }

    @Override
    public void delete() {
        if (!MessageBox.confirm(ShellI18nHelper.confirm("deleteView", this.value.getName()), this.getWindow())) {
            return;
        }
        try {
            this.dbItem().dropView(this.value);
            ShellPostgresqlEventUtil.dropView(this);
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
    public ShellPostgresqlSchemaTreeItem dbItem() {
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
        param.setStart(pageNo * limit);
        param.setSchema(this.schema());
        param.setTableName(this.viewName());
        List<PostgresqlRecord> records = this.client().viewRecords(this.schema(), this.viewName(), pageNo * limit, limit, filters);
        long count = this.client().selectRecordCount(param);
        Paging<PostgresqlRecord> paging = new Paging<>(records, limit, count);
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
    public PostgresqlColumns columns() {
        return new PostgresqlColumns(this.client().viewColumns(this.schema(), this.viewName()));
    }

    @Override
    public void onPrimaryDoubleClick() {
        ShellPostgresqlEventUtil.viewOpen(this, this.dbItem());
    }

    /**
     * 获取主键列，优先返回自动递增列
     *
     * @return 主键列
     */
    public PostgresqlColumn getPrimaryKey() {
        if (this.value.getColumns() == null) {
            this.viewColumns();
        }
        PostgresqlColumn dbColumn = null;
        if (this.value.columns() != null) {
            for (PostgresqlColumn column : this.value.columns()) {
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
    public int insertRecord(DBRecordData recordData, PostgresqlRecordPrimaryKey primaryKey) {
        PostgresqlInsertRecordParam param = new PostgresqlInsertRecordParam();
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
        PostgresqlDeleteRecordParam param = new PostgresqlDeleteRecordParam();
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
    public int deleteRecord(PostgresqlRecordPrimaryKey primaryKey) {
        PostgresqlDeleteRecordParam param = new PostgresqlDeleteRecordParam();
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
    public PostgresqlRecord selectRecord(PostgresqlRecordPrimaryKey primaryKey) {
        PostgresqlSelectRecordParam param = new PostgresqlSelectRecordParam();
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
    public int updateRecord(DBRecordData recordData, PostgresqlRecordPrimaryKey primaryKey) {
        PostgresqlUpdateRecordParam param = new PostgresqlUpdateRecordParam();
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
        PostgresqlUpdateRecordParam param = new PostgresqlUpdateRecordParam();
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
            ShellPostgresqlEventUtil.viewRenamed(oldName, newName, this.dbItem());
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex, this.getWindow());
        }
    }
}
