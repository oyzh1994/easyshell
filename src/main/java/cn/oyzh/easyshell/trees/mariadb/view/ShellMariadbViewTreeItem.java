package cn.oyzh.easyshell.trees.mariadb.view;

import cn.oyzh.common.dto.Paging;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.event.mariadb.ShellMariadbEventUtil;
import cn.oyzh.easyshell.mariadb.ShellMariadbClient;
import cn.oyzh.easyshell.mariadb.column.MariadbColumn;
import cn.oyzh.easyshell.mariadb.column.MariadbColumns;
import cn.oyzh.easyshell.mariadb.record.MariadbDeleteRecordParam;
import cn.oyzh.easyshell.mariadb.record.MariadbInsertRecordParam;
import cn.oyzh.easyshell.mariadb.record.MariadbRecord;
import cn.oyzh.easyshell.mariadb.record.MariadbRecordFilter;
import cn.oyzh.easyshell.mariadb.record.MariadbRecordPrimaryKey;
import cn.oyzh.easyshell.mariadb.record.MariadbSelectRecordParam;
import cn.oyzh.easyshell.mariadb.record.MariadbUpdateRecordParam;
import cn.oyzh.easyshell.mariadb.view.MariadbView;
import cn.oyzh.easyshell.trees.mariadb.ShellMariadbTreeItem;
import cn.oyzh.easyshell.trees.mariadb.database.ShellMariadbDatabaseTreeItem;
import cn.oyzh.easyshell.util.mariadb.ShellMariadbViewFactory;
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
 * MariaDB视图节点
 *
 * @author oyzh
 * @since 2026-10-09
 */
public class ShellMariadbViewTreeItem extends ShellMariadbTreeItem<ShellMariadbViewTreeItemValue> {

    /**
     * 当前值
     */
    private final MariadbView value;

    /**
     * 获取视图对象
     *
     * @return 视图对象
     */
    public MariadbView value() {
        return value;
    }

    /**
     * 构造视图节点
     *
     * @param view     视图对象
     * @param treeView 树视图
     */
    public ShellMariadbViewTreeItem(MariadbView view, RichTreeView treeView) {
        super(treeView);
        super.setFilterable(true);
        this.value = view;
        this.setValue(new ShellMariadbViewTreeItemValue(this));
    }

    @Override
    public ShellMariadbViewsTreeItem parent() {
        return (ShellMariadbViewsTreeItem) super.parent();
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
     * 获取MariaDB信息
     *
     * @return MariaDB信息
     */
    public ShellConnect info() {
        return this.parent().info();
    }

    /**
     * 获取视图列信息
     *
     * @return 视图列信息
     */
    public MariadbColumns viewColumns() {
        this.value.setColumns(new MariadbColumns(this.columns()));
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
            MariadbView mariadbView = this.dbItem().selectView(cloneView);
            this.dbItem().getViewTypeChild().addView(mariadbView);
        } catch (Exception ex) {
            MessageBox.exception(ex);
        }
    }

    /**
     * 查看视图信息
     */
    private void viewInfo() {
        ShellMariadbViewFactory.viewInfo(this);
    }

    /**
     * 设计视图
     */
    private void designView() {
        ShellMariadbEventUtil.designView(this.value, this.dbItem());
    }

    @Override
    public void delete() {
        if (!MessageBox.confirm(I18nHelper.deleteView() + " " + this.value.getName() + "?")) {
            return;
        }
        try {
            this.dbItem().dropView(this.value);
            ShellMariadbEventUtil.dropView(this);
            this.parent().clearViewSize();
            super.remove();
        } catch (Exception ex) {
            MessageBox.exception(ex);
        }
    }

    /**
     * 获取所属数据库节点
     *
     * @return 数据库节点
     */
    public ShellMariadbDatabaseTreeItem dbItem() {
        return this.parent().parent();
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
    public Paging<MariadbRecord> recordPage(long pageNo, long limit, List<MariadbRecordFilter> filters, List<MariadbColumn> columns) {
        MariadbSelectRecordParam param = new MariadbSelectRecordParam();
        param.setLimit(limit);
        param.setFilters(filters);
        param.setColumns(columns);
        param.setStart(pageNo * limit);
        param.setDbName(this.dbName());
        param.setTableName(this.viewName());
        List<MariadbRecord> records = this.client().viewRecords(this.dbName(), this.viewName(), pageNo * limit, limit, filters);
        long count = this.client().selectRecordCount(param);
        Paging<MariadbRecord> paging = new Paging<>(records, limit, count);
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
     * 获取视图列列表
     *
     * @return 视图列列表
     */
    public MariadbColumns columns() {
        return new MariadbColumns(this.client().viewColumns(this.dbName(), this.viewName()));
    }

    @Override
    public void onPrimaryDoubleClick() {
        ShellMariadbEventUtil.viewOpen(this, this.dbItem());
    }

    /**
     * 获取主键列，优先返回自动递增列
     *
     * @return 主键列
     */
    public MariadbColumn getPrimaryKey() {
        if (this.value.getColumns() == null) {
            this.viewColumns();
        }
        MariadbColumn dbColumn = null;
        if (this.value.columns() != null) {
            for (MariadbColumn column : this.value.columns()) {
                if (column.isAutoIncrement()) {
                    dbColumn = column;
                    break;
                }
            }
        }
        return dbColumn;
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
     * 获取视图名称
     *
     * @return 视图名称
     */
    public String viewName() {
        return this.value.getName();
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
    public int insertRecord(DBRecordData recordData, MariadbRecordPrimaryKey primaryKey) {
        MariadbInsertRecordParam param = new MariadbInsertRecordParam();
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
        MariadbDeleteRecordParam param = new MariadbDeleteRecordParam();
        param.setRecord(recordData);
        param.setDbName(this.dbName());
        param.setTableName(this.viewName());
        return this.client().deleteRecord(param);
    }

    /**
     * 删除视图记录
     *
     * @param primaryKey 主键
     * @return 受影响行数
     */
    public int deleteRecord(MariadbRecordPrimaryKey primaryKey) {
        MariadbDeleteRecordParam param = new MariadbDeleteRecordParam();
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
    public MariadbRecord selectRecord(MariadbRecordPrimaryKey primaryKey) {
        MariadbSelectRecordParam param = new MariadbSelectRecordParam();
        param.setDbName(this.dbName());
        param.setTableName(this.viewName());
        param.setPrimaryKey(primaryKey);
        return this.client().selectRecord(param);
    }

    /**
     * 更新视图记录
     *
     * @param recordData 记录数据
     * @param primaryKey 主键
     * @return 受影响行数
     */
    public int updateRecord(DBRecordData recordData, MariadbRecordPrimaryKey primaryKey) {
        MariadbUpdateRecordParam param = new MariadbUpdateRecordParam();
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
        MariadbUpdateRecordParam param = new MariadbUpdateRecordParam();
        param.setDbName(this.dbName());
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
            ShellMariadbEventUtil.viewRenamed(oldName, newName, this.dbItem());
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex);
        }
    }
}
