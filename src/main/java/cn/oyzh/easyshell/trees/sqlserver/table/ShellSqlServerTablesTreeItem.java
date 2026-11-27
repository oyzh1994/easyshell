package cn.oyzh.easyshell.trees.sqlserver.table;

import cn.oyzh.common.thread.Task;
import cn.oyzh.common.thread.TaskBuilder;
import cn.oyzh.easyshell.sqlserver.ShellSqlServerClient;
import cn.oyzh.easyshell.sqlserver.table.SqlServerTable;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.event.sqlserver.ShellSqlServerEventUtil;
import cn.oyzh.easyshell.trees.sqlserver.ShellSqlServerTreeItem;
import cn.oyzh.easyshell.trees.sqlserver.schema.ShellSqlServerSchemaTreeItem;
import cn.oyzh.easyshell.util.sqlserver.ShellSqlServerViewFactory;
import cn.oyzh.fx.gui.menu.MenuItemHelper;
import cn.oyzh.fx.gui.tree.view.RichTreeView;
import cn.oyzh.fx.plus.information.MessageBox;
import cn.oyzh.fx.plus.menu.FXMenuItem;
import javafx.collections.ObservableList;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeItem;

import java.util.ArrayList;
import java.util.List;

/**
 * SqlServer数据库树表类型节点
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class ShellSqlServerTablesTreeItem extends ShellSqlServerTreeItem<ShellSqlServerTablesTreeItemValue> {

    /**
     * 构造SqlServer数据库树表类型节点
     *
     * @param treeView 树视图
     */
    public ShellSqlServerTablesTreeItem(RichTreeView treeView) {
        super(treeView);
        super.setFilterable(true);
        this.setValue(new ShellSqlServerTablesTreeItemValue(this));
    }

    @Override
    public ShellSqlServerSchemaTreeItem parent() {
        return (ShellSqlServerSchemaTreeItem) super.parent();
    }

    @Override
    public List<MenuItem> getMenuItems() {
        List<MenuItem> items = new ArrayList<>();
        FXMenuItem reload = MenuItemHelper.reloadData(this::reloadChild);
        FXMenuItem add = MenuItemHelper.addTable(this::addTable);
        FXMenuItem exportData = MenuItemHelper.exportData(this::exportData);
        FXMenuItem importData = MenuItemHelper.importData(this::importData);
        items.add(add);
        items.add(reload);
        items.add(exportData);
        items.add(importData);
        return items;
    }

    /**
     * 导出数据
     */
    private void exportData() {
        ShellSqlServerViewFactory.exportData(this.client(), this.schema(), null);
    }

    /**
     * 导入数据
     */
    private void importData() {
        ShellSqlServerViewFactory.importData(this.client(), this.schema());
    }

    /**
     * 新增表
     */
    private void addTable() {
        SqlServerTable table = new SqlServerTable();
        table.setSchema(this.schema());
        ShellSqlServerEventUtil.designTable(table, this.parent());
    }

    @Override
    public boolean itemVisible() {
        return this.isVisible();
    }

    @Override
    public void loadChild() {
        if (!this.isLoading() && !this.isLoaded()) {
            this.setLoaded(true);
            this.setLoading(true);
            Task task = TaskBuilder.newBuilder()
                    .onStart(() -> {
                        List<SqlServerTable> tables = this.client().selectTablesSimple(this.schema());
                        // 无数据直接更新列表
                        if (this.isChildEmpty()) {
                            List<TreeItem<?>> list = new ArrayList<>();
                            for (SqlServerTable table : tables) {
                                list.add(new ShellSqlServerTableTreeItem(table, this.getTreeView()));
                            }
                            this.setChild(list);
                        } else {// 有数据则执行删除、新增、更新操作
                            ObservableList children = this.richChildren();
                            ObservableList<ShellSqlServerTableTreeItem> list = children;
                            List<ShellSqlServerTableTreeItem> delList = new ArrayList<>();
                            List<ShellSqlServerTableTreeItem> addList = new ArrayList<>();
                            // 删除
                            for (ShellSqlServerTableTreeItem item : list) {
                                if (tables.parallelStream().noneMatch(f -> f.compare(item.value()))) {
                                    delList.add(item);
                                }
                            }
                            // 新增
                            for (SqlServerTable table : tables) {
                                if (list.parallelStream().noneMatch(item -> table.compare(item.value()))) {
                                    addList.add(new ShellSqlServerTableTreeItem(table, this.getTreeView()));
                                }
                            }
                            // 更新
                            for (ShellSqlServerTableTreeItem item : list) {
                                if (!addList.contains(item) && !delList.contains(item)) {
                                    tables.parallelStream().filter(f -> f.compare(item.value())).findFirst().ifPresent(f -> item.value().copy(f));
                                }
                            }
                            list.removeAll(delList);
                            list.addAll(addList);
                        }
                    })
                    .onSuccess(this::expend)
                    .onError(ex -> {
                        this.setLoaded(false);
                        MessageBox.exception(ex, this.getWindow());
                    })
                    .onFinish(() -> {
                        this.setLoading(false);
                        this.doFilter();
                        this.doSort();
                    })
                    .build();
            // 执行业务
            this.startWaiting(task);
        }
    }

    @Override
    public void reloadChild() {
//        this.clearTableSize();
        this.clearChild();
        this.setLoaded(false);
        this.loadChild();
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
     * 获取SqlServer数据库客户端
     *
     * @return SqlServer数据库客户端
     */
    public ShellSqlServerClient client() {
        return this.parent().client();
    }

    // [已停用] 旧的数量查询实现（保留备用）
//    /**
//     * 获取表数量
//     *
//     * @return 表数量
//     */
//    public int tableSize() {
//        try {
//            return this.parent().tableSize();
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
//        return 0;
//    }

    // [已停用] 旧的数量缓存字段（保留备用）
//    /**
//     * 表数量
//     */
//    private Integer tableSize;

    /**
     * 获取表数量
     * <p>
     * 懒加载：仅在表列表加载完成后直接使用已加载的子节点数量，避免打开模式时执行数量查询导致卡顿。
     *
     * @return 表数量，未加载时返回null表示暂无数量
     */
    public Integer getTableSize() {
        // 未加载或加载中不显示数量，数量取自已加载的子节点，不访问数据库
        if (!this.isLoaded() || this.isLoading()) {
            return null;
        }
        return this.unfilteredChildrenSize();
    }

    // [已停用] 旧的缓存数量实现（保留备用）
//    /**
//     * 获取表数量
//     *
//     * @return 表数量
//     */
//    public synchronized Integer getTableSize() {
//        if (this.tableSize == null) {
//            this.tableSize = this.tableSize();
//        }
//        return this.tableSize;
//    }

    /**
     * 获取连接信息
     *
     * @return 连接信息
     */
    public ShellConnect info() {
        return this.parent().info();
    }

    /**
     * 获取连接名称
     *
     * @return 连接名称
     */
    public String infoName() {
        return this.parent().infoName();
    }

    @Override
    public void onPrimaryDoubleClick() {
        if (!this.isLoaded()) {
            this.loadChild();
        } else {
            super.onPrimaryDoubleClick();
        }
    }

    /**
     * 添加表
     *
     * @param table 表
     */
    public void addTable(SqlServerTable table) {
        this.addChild(new ShellSqlServerTableTreeItem(table, this.getTreeView()));
        this.sortChild(this.isSortAsc());
//        this.clearTableSize();
    }

    // [已停用] 旧的缓存清理实现（保留备用）
//    /**
//     * 清空表数量缓存
//     */
//    public void clearTableSize() {
//        this.tableSize = null;
//    }
}
