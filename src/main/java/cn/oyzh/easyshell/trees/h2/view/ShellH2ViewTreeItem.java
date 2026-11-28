package cn.oyzh.easyshell.trees.h2.view;

import cn.oyzh.common.dto.Paging;
import cn.oyzh.common.util.StringUtil;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.event.h2.ShellH2EventUtil;
import cn.oyzh.easyshell.h2.column.H2Column;
import cn.oyzh.easyshell.h2.column.H2Columns;
import cn.oyzh.easyshell.h2.record.H2DeleteRecordParam;
import cn.oyzh.easyshell.h2.record.H2InsertRecordParam;
import cn.oyzh.easyshell.h2.record.H2Record;
import cn.oyzh.easyshell.h2.record.H2RecordFilter;
import cn.oyzh.easyshell.h2.record.H2RecordPrimaryKey;
import cn.oyzh.easyshell.h2.record.H2SelectRecordParam;
import cn.oyzh.easyshell.h2.record.H2UpdateRecordParam;
import cn.oyzh.easyshell.h2.view.H2View;
import cn.oyzh.easyshell.h2.view.H2ViewUpdateMapping;
import cn.oyzh.easyshell.h2.ShellH2Client;
import cn.oyzh.easyshell.trees.h2.ShellH2TreeItem;
import cn.oyzh.easyshell.trees.h2.root.ShellH2RootTreeItem;
import cn.oyzh.easyshell.util.h2.ShellH2ViewFactory;
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
 * H2视图节点
 *
 * @author oyzh
 * @since 2026-11-02
 */
public class ShellH2ViewTreeItem extends ShellH2TreeItem<ShellH2ViewTreeItemValue> {

    /**
     * 当前值
     */
    private final H2View value;

    /**
     * 构造视图节点
     *
     * @param view     视图对象
     * @param treeView 树视图
     */
    public ShellH2ViewTreeItem(H2View view, RichTreeView treeView) {
        super(treeView);
        super.setFilterable(true);
        this.value = view;
        this.setValue(new ShellH2ViewTreeItemValue(this));
    }

    @Override
    public ShellH2ViewsTreeItem parent() {
        return (ShellH2ViewsTreeItem) super.parent();
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
     * 获取视图对象
     *
     * @return 视图对象
     */
    public H2View value() {
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
    public H2Columns viewColumns() {
        H2Columns columns = new H2Columns(
                this.client().viewColumns(this.dbName(), this.viewName()));
        this.value.setColumns(columns);
        H2ViewUpdateMapping mapping = this.client().viewUpdateMapping(
                this.dbName(), this.viewName(), columns);
        this.value.setUpdateMapping(mapping);
        if (mapping != null) {
            for (H2Column column : columns) {
                if (mapping.isVirtualColumn(column.getName())) {
                    column.setGeneratedExpression(
                            mapping.virtualExpression(column.getName()));
                }
            }
        }
        return this.value.getColumns();
    }

    /**
     * 获取视图列列表
     *
     * @return 视图列列表
     */
    public H2Columns columns() {
        // 视图定义可能被设计器、终端或 SQL 修改，字段列表必须实时读取
        return this.viewColumns();
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
     * 视图是否可更新
     *
     * @return 是否可更新
     */
    public boolean isUpdatable() {
        if (this.value.getUpdateMapping() == null) {
            this.columns();
        }
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
    public Paging<H2Record> recordPage(long pageNo, long limit, List<H2RecordFilter> filters,
                                           List<H2Column> columns) {
        H2SelectRecordParam param = new H2SelectRecordParam();
        param.setLimit(limit);
        param.setReadonly(!this.isUpdatable());
        param.setFilters(filters);
        param.setColumns(columns);
        param.setDbName(this.dbName());
        param.setStart(pageNo * limit);
        param.setTableName(this.viewName());
        List<H2Record> records = this.client().selectRecords(param);
        long count = this.client().selectRecordCount(param);
        Paging<H2Record> paging = new Paging<>(records, limit, count);
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
    public int insertRecord(DBRecordData recordData, H2RecordPrimaryKey primaryKey) {
        H2ViewUpdateMapping mapping = this.requireUpdateMapping();
        H2RecordPrimaryKey sourcePrimaryKey = this.mapPrimaryKey(primaryKey, mapping);
        H2InsertRecordParam param = new H2InsertRecordParam();
        param.setRecord(this.mapRecordData(recordData, mapping));
        param.setDbName(this.dbName());
        param.setPrimaryKey(sourcePrimaryKey);
        param.setTableName(mapping.getTableName());
        int result = this.client().insertRecord(param);
        if (primaryKey != null && sourcePrimaryKey != null) {
            primaryKey.setReturnData(sourcePrimaryKey.getReturnData());
        }
        return result;
    }

    /**
     * 删除视图记录
     *
     * @param recordData 记录数据
     * @return 受影响行数
     */
    public int deleteRecord(DBRecordData recordData) {
        H2ViewUpdateMapping mapping = this.requireUpdateMapping();
        H2DeleteRecordParam param = new H2DeleteRecordParam();
        param.setDbName(this.dbName());
        param.setTableName(mapping.getTableName());
        param.setRecord(this.mapRecordData(recordData, mapping));
        return this.client().deleteRecord(param);
    }

    /**
     * 删除视图记录
     *
     * @param primaryKey 主键
     * @return 受影响行数
     */
    public int deleteRecord(H2RecordPrimaryKey primaryKey) {
        H2ViewUpdateMapping mapping = this.requireUpdateMapping();
        H2DeleteRecordParam param = new H2DeleteRecordParam();
        param.setDbName(this.dbName());
        param.setTableName(mapping.getTableName());
        param.setPrimaryKey(this.mapPrimaryKey(primaryKey, mapping));
        return this.client().deleteRecord(param);
    }

    /**
     * 查询单条视图记录
     *
     * @param primaryKey 主键
     * @return 视图记录
     */
    public H2Record selectRecord(H2RecordPrimaryKey primaryKey) {
        H2SelectRecordParam param = new H2SelectRecordParam();
        param.setDbName(this.dbName());
        param.setTableName(this.viewName());
        param.setColumns(this.columns());
        param.setPrimaryKey(primaryKey);
        param.setReadonly(!this.isUpdatable());
        return this.client().selectRecord(param);
    }

    /**
     * 更新视图记录
     *
     * @param recordData 记录数据
     * @param primaryKey 主键
     * @return 受影响行数
     */
    public int updateRecord(DBRecordData recordData, H2RecordPrimaryKey primaryKey) {
        H2ViewUpdateMapping mapping = this.requireUpdateMapping();
        H2UpdateRecordParam param = new H2UpdateRecordParam();
        param.setDbName(this.dbName());
        param.setTableName(mapping.getTableName());
        param.setPrimaryKey(this.mapPrimaryKey(primaryKey, mapping));
        param.setUpdateRecord(this.mapRecordData(recordData, mapping));
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
        H2ViewUpdateMapping mapping = this.requireUpdateMapping();
        H2UpdateRecordParam param = new H2UpdateRecordParam();
        param.setDbName(this.dbName());
        param.setTableName(mapping.getTableName());
        param.setUpdateRecord(this.mapRecordData(recordData, mapping));
        param.setRecord(this.mapRecordData(originalRecordData, mapping));
        return this.client().updateRecord(param);
    }

    private H2ViewUpdateMapping requireUpdateMapping() {
        H2ViewUpdateMapping mapping = this.value.getUpdateMapping();
        if (mapping == null) {
            this.columns();
            mapping = this.value.getUpdateMapping();
        }
        if (mapping == null) {
            throw new IllegalStateException("H2 view is not updatable: " + this.viewName());
        }
        return mapping;
    }

    private DBRecordData mapRecordData(DBRecordData recordData, H2ViewUpdateMapping mapping) {
        if (recordData == null) {
            return null;
        }
        List<H2Column> sourceColumns = this.client().selectColumns(
                this.dbName(), mapping.getTableName());
        DBRecordData result = new DBRecordData();
        for (var entry : recordData.entrySet()) {
            String sourceName = mapping.sourceColumn(entry.getKey().getName());
            H2Column sourceColumn = sourceColumns.stream()
                    .filter(column -> StringUtil.equalsIgnoreCase(column.getName(), sourceName))
                    .findFirst().orElse(null);
            if (sourceColumn != null) {
                result.put(sourceColumn, entry.getValue());
            }
        }
        return result;
    }

    private H2RecordPrimaryKey mapPrimaryKey(H2RecordPrimaryKey primaryKey,
                                             H2ViewUpdateMapping mapping) {
        if (primaryKey == null) {
            return null;
        }
        String sourceName = mapping.sourceColumn(primaryKey.getColumnName());
        if (StringUtil.isBlank(sourceName)) {
            return null;
        }
        H2RecordPrimaryKey result = new H2RecordPrimaryKey();
        result.setColumnName(sourceName);
        result.setData(primaryKey.getData());
        result.setOriginalData(primaryKey.getOriginalData());
        result.setAutoIncrement(this.client().selectColumns(
                        this.dbName(), mapping.getTableName()).stream()
                .filter(column -> StringUtil.equalsIgnoreCase(column.getName(), sourceName))
                .findFirst()
                .map(H2Column::isAutoIncrement)
                .orElse(false));
        return result;
    }

    @Override
    public void onPrimaryDoubleClick() {
        ShellH2EventUtil.viewOpen(this.client(), this.dbName(), this.viewName());
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
        ShellH2EventUtil.designView(this.client(), this.dbName(), this.viewName());
    }

    /**
     * 视图信息
     */
    private void viewInfo() {
        ShellH2ViewFactory.viewInfo(this);
    }

    /**
     * 导出数据
     */
    private void exportData() {
        ShellH2ViewFactory.exportData(this.client(), this.viewName());
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
            this.client().renameView(this.dbName(), oldName, name);
            this.value.setName(name);
            this.value.setColumns(null);
            this.refresh();
            this.parent().doSort();
//            this.parent().clearViewSize();
            ShellH2EventUtil.viewChanged(this.client(), this.dbName(), oldName, name);
        } catch (Exception ex) {
            ex.printStackTrace();
            MessageBox.exception(ex, this.getWindow());
        }
    }

    @Override
    public void delete() {
        if (!MessageBox.confirm(ShellI18nHelper.confirmDeleteView(this.viewName()), this.getWindow())) {
            return;
        }
        try {
            this.client().dropView(this.dbName(), this.value);
//            this.parent().clearViewSize();
            ShellH2EventUtil.viewChanged(this.client(), this.dbName(), this.viewName(), null);
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
                ShellH2EventUtil.viewChanged(this.client(), this.dbName(), null, cloneName);
            } catch (Exception ex) {
                ex.printStackTrace();
                MessageBox.exception(ex, this.getWindow());
            }
        });
    }
}
