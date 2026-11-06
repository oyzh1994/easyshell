package cn.oyzh.easyshell.trees.postgresql.table;

import cn.oyzh.common.thread.Task;
import cn.oyzh.common.thread.TaskBuilder;
import cn.oyzh.easyshell.postgresql.ShellPostgresqlClient;
import cn.oyzh.easyshell.postgresql.table.PostgresqlTable;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.event.postgresql.ShellPostgresqlEventUtil;
import cn.oyzh.easyshell.trees.postgresql.ShellPostgresqlTreeItem;
import cn.oyzh.easyshell.trees.postgresql.schema.ShellPostgresqlSchemaTreeItem;
import cn.oyzh.easyshell.util.postgresql.ShellPostgresqlViewFactory;
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
 * PostgreSQL数据库树表类型节点
 *
 * @author oyzh
 * @since 2026-09-02
 */
public class ShellPostgresqlTablesTreeItem extends ShellPostgresqlTreeItem<ShellPostgresqlTablesTreeItemValue> {

    /**
     * 构造PostgreSQL数据库树表类型节点
     *
     * @param treeView 树视图
     */
    public ShellPostgresqlTablesTreeItem(RichTreeView treeView) {
        super(treeView);
        super.setFilterable(true);
        this.setValue(new ShellPostgresqlTablesTreeItemValue(this));
    }

    @Override
    public ShellPostgresqlSchemaTreeItem parent() {
        return (ShellPostgresqlSchemaTreeItem) super.parent();
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
        ShellPostgresqlViewFactory.exportData(this.client(), this.schema(), null);
    }

    /**
     * 导入数据
     */
    private void importData() {
        ShellPostgresqlViewFactory.importData(this.client(), this.schema());
    }

    /**
     * 新增表
     */
    private void addTable() {
        PostgresqlTable table = new PostgresqlTable();
        table.setSchema(this.schema());
        ShellPostgresqlEventUtil.designTable(table, this.parent());
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
                        List<PostgresqlTable> tables = this.client().selectTablesSimple(this.schema());
                        // 无数据直接更新列表
                        if (this.isChildEmpty()) {
                            List<TreeItem<?>> list = new ArrayList<>();
                            for (PostgresqlTable table : tables) {
                                list.add(new ShellPostgresqlTableTreeItem(table, this.getTreeView()));
                            }
                            this.setChild(list);
                        } else {// 有数据则执行删除、新增、更新操作
                            ObservableList children = this.richChildren();
                            ObservableList<ShellPostgresqlTableTreeItem> list = children;
                            List<ShellPostgresqlTableTreeItem> delList = new ArrayList<>();
                            List<ShellPostgresqlTableTreeItem> addList = new ArrayList<>();
                            // 删除
                            for (ShellPostgresqlTableTreeItem item : list) {
                                if (tables.parallelStream().noneMatch(f -> f.compare(item.value()))) {
                                    delList.add(item);
                                }
                            }
                            // 新增
                            for (PostgresqlTable table : tables) {
                                if (list.parallelStream().noneMatch(item -> table.compare(item.value()))) {
                                    addList.add(new ShellPostgresqlTableTreeItem(table, this.getTreeView()));
                                }
                            }
                            // 更新
                            for (ShellPostgresqlTableTreeItem item : list) {
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
        this.clearTableSize();
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
     * 获取PostgreSQL数据库客户端
     *
     * @return PostgreSQL数据库客户端
     */
    public ShellPostgresqlClient client() {
        return this.parent().client();
    }

    /**
     * 获取表数量
     *
     * @return 表数量
     */
    public int tableSize() {
        try {
            return this.parent().tableSize();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return 0;
    }

    /**
     * 表数量
     */
    private Integer tableSize;

    /**
     * 获取表数量
     *
     * @return 表数量
     */
    public Integer getTableSize() {
        if (this.tableSize == null) {
            this.tableSize = this.tableSize();
        }
        return this.tableSize;
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
    public void addTable(PostgresqlTable table) {
        this.addChild(new ShellPostgresqlTableTreeItem(table, this.getTreeView()));
        this.sortChild(this.isSortAsc());
        this.clearTableSize();
    }

    /**
     * 清空表数量缓存
     */
    public void clearTableSize() {
        this.tableSize = null;
    }
}
