package cn.oyzh.easyshell.trees.dameng.view;

import cn.oyzh.common.dto.Paging;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.dameng.ShellDamengClient;
import cn.oyzh.easyshell.dameng.column.DamengColumn;
import cn.oyzh.easyshell.dameng.column.DamengColumns;
import cn.oyzh.easyshell.dameng.record.DamengDeleteRecordParam;
import cn.oyzh.easyshell.dameng.record.DamengInsertRecordParam;
import cn.oyzh.easyshell.dameng.record.DamengRecord;
import cn.oyzh.easyshell.dameng.record.DamengRecordFilter;
import cn.oyzh.easyshell.dameng.record.DamengRecordPrimaryKey;
import cn.oyzh.easyshell.dameng.record.DamengSelectRecordParam;
import cn.oyzh.easyshell.dameng.record.DamengUpdateRecordParam;
import cn.oyzh.easyshell.dameng.view.DamengView;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.event.dameng.ShellDamengEventUtil;
import cn.oyzh.easyshell.trees.dameng.ShellDamengTreeItem;
import cn.oyzh.easyshell.trees.dameng.schema.ShellDamengSchemaTreeItem;
import cn.oyzh.easyshell.util.dameng.ShellDamengViewFactory;
import cn.oyzh.fx.db.DBRecordData;
import cn.oyzh.fx.db.util.DBUtil;
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
 * 达梦数据库树视图节点
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class ShellDamengViewTreeItem extends ShellDamengTreeItem<ShellDamengViewTreeItemValue> {

    /**
     * 当前值
     */
    private final DamengView value;

    /**
     * 获取视图值
     *
     * @return 视图值
     */
    public DamengView value() {
        return value;
    }

    /**
     * 构造达梦数据库树视图节点
     *
     * @param view     视图
     * @param treeView 树视图
     */
    public ShellDamengViewTreeItem(DamengView view, RichTreeView treeView) {
        super(treeView);
        super.setFilterable(true);
        this.value = view;
        this.setValue(new ShellDamengViewTreeItemValue(this));
    }

    @Override
    public ShellDamengViewsTreeItem parent() {
        return (ShellDamengViewsTreeItem) super.parent();
    }

    /**
     * 获取达梦数据库客户端
     *
     * @return 达梦数据库客户端
     */
    public ShellDamengClient client() {
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
    public DamengColumns viewColumns() {
        this.value.setColumns(new DamengColumns(this.columns()));
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
        StageManager.showMask(this::doCloneView);
    }

    /**
     * 执行克隆视图
     */
    private void doCloneView() {
        try {
            String cloneView = this.viewName() + DBUtil.genCloneName();
            this.dbItem().cloneView(this.viewName(), cloneView);
            DamengView view = this.dbItem().selectView(cloneView);
            this.dbItem().getViewTypeChild().addView(view);
        } catch (Exception ex) {
            MessageBox.exception(ex);
        }
    }

    /**
     * 视图信息
     */
    private void viewInfo() {
        ShellDamengViewFactory.viewInfo(this);
    }

    /**
     * 设计视图
     */
    private void designView() {
        ShellDamengEventUtil.designView(this.value, this.dbItem());
    }

    @Override
    public void delete() {
        if (!MessageBox.confirm(I18nHelper.deleteView() + " " + this.value.getName() + "?")) {
            return;
        }
        try {
            this.dbItem().dropView(this.value);
            ShellDamengEventUtil.dropView(this);
            this.parent().clearViewSize();
            super.remove();
        } catch (Exception ex) {
            MessageBox.exception(ex);
        }
    }

    /**
     * 获取所属模式节点
     *
     * @return 模式节点
     */
    public ShellDamengSchemaTreeItem dbItem() {
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
    public Paging<DamengRecord> recordPage(long pageNo, long limit, List<DamengRecordFilter> filters, List<DamengColumn> columns) {
        DamengSelectRecordParam param = new DamengSelectRecordParam();
        param.setLimit(limit);
        param.setFilters(filters);
        param.setColumns(columns);
        param.setStart(pageNo * limit);
        param.setSchema(this.schema());
        param.setTableName(this.viewName());
        List<DamengRecord> records = this.client().viewRecords(this.schema(), this.viewName(), pageNo * limit, limit, filters);
        long count = this.client().selectRecordCount(param);
        Paging<DamengRecord> paging = new Paging<>(records, limit, count);
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
    public DamengColumns columns() {
        return new DamengColumns(this.client().viewColumns(this.schema(), this.viewName()));
    }

    @Override
    public void onPrimaryDoubleClick() {
        ShellDamengEventUtil.viewOpen(this, this.dbItem());
    }

    /**
     * 获取主键列，优先返回自动递增列
     *
     * @return 主键列
     */
    public DamengColumn getPrimaryKey() {
        if (this.value.getColumns() == null) {
            this.viewColumns();
        }
        DamengColumn dbColumn = null;
        if (this.value.columns() != null) {
            for (DamengColumn column : this.value.columns()) {
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
    public int insertRecord(DBRecordData recordData, DamengRecordPrimaryKey primaryKey) {
        DamengInsertRecordParam param = new DamengInsertRecordParam();
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
        DamengDeleteRecordParam param = new DamengDeleteRecordParam();
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
    public int deleteRecord(DamengRecordPrimaryKey primaryKey) {
        DamengDeleteRecordParam param = new DamengDeleteRecordParam();
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
    public DamengRecord selectRecord(DamengRecordPrimaryKey primaryKey) {
        DamengSelectRecordParam param = new DamengSelectRecordParam();
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
    public int updateRecord(DBRecordData recordData, DamengRecordPrimaryKey primaryKey) {
        DamengUpdateRecordParam param = new DamengUpdateRecordParam();
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
        DamengUpdateRecordParam param = new DamengUpdateRecordParam();
        param.setSchema(this.schema());
        param.setTableName(this.viewName());
        param.setUpdateRecord(recordData);
        param.setRecord(originalRecordData);
        return this.client().updateRecord(param);
    }

    @Override
    public void rename() {
        try {
            String newName = MessageBox.prompt(I18nHelper.pleaseInputName(), this.viewName());
            // 名称为null或者跟当前名称相同，则忽略
            if (newName == null || Objects.equals(newName, this.viewName())) {
                return;
            }
            // 检查名称
            if (StringUtil.isBlank(newName)) {
                MessageBox.warn(I18nHelper.pleaseInputContent());
                return;
            }
            String oldName = this.viewName();
            // 修改名称
            this.dbItem().renameView(oldName, newName);
            this.value.setName(newName);
            this.refresh();
            ShellDamengEventUtil.viewRenamed(oldName, newName, this.dbItem());
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }
}
