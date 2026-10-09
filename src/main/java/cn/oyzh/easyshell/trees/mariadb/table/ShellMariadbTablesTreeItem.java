package cn.oyzh.easyshell.trees.mariadb.table;

import cn.oyzh.common.thread.Task;
import cn.oyzh.common.thread.TaskBuilder;
import cn.oyzh.easyshell.domain.ShellConnect;
import cn.oyzh.easyshell.event.mariadb.ShellMariadbEventUtil;
import cn.oyzh.easyshell.mariadb.ShellMariadbClient;
import cn.oyzh.easyshell.mariadb.table.MariadbTable;
import cn.oyzh.easyshell.trees.mariadb.ShellMariadbTreeItem;
import cn.oyzh.easyshell.trees.mariadb.database.ShellMariadbDatabaseTreeItem;
import cn.oyzh.easyshell.util.mariadb.ShellMariadbViewFactory;
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
 * MariaDB表类型节点
 *
 * @author oyzh
 * @since 2026-10-09
 */
public class ShellMariadbTablesTreeItem extends ShellMariadbTreeItem<ShellMariadbTablesTreeItemValue> {

    /**
     * 构造表类型节点
     *
     * @param treeView 树视图
     */
    public ShellMariadbTablesTreeItem(RichTreeView treeView) {
        super(treeView);
        super.setFilterable(true);
        this.setValue(new ShellMariadbTablesTreeItemValue(this));
    }

    @Override
    public ShellMariadbDatabaseTreeItem parent() {
        return (ShellMariadbDatabaseTreeItem) super.parent();
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
        ShellMariadbViewFactory.exportData(this.client(), this.dbName(), null);
    }

    /**
     * 导入数据
     */
    private void importData() {
        ShellMariadbViewFactory.importData(this.client(), this.dbName());
    }

    /**
     * 新增表
     */
    private void addTable() {
        MariadbTable table = new MariadbTable();
        table.setDbName(this.dbName());
        ShellMariadbEventUtil.designTable(table, this.parent());
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
                        List<MariadbTable> tables = this.client().selectTablesSimple(this.dbName());
                        // 无数据直接更新列表
                        if (this.isChildEmpty()) {
                            List<TreeItem<?>> list = new ArrayList<>();
                            for (MariadbTable table : tables) {
                                list.add(new ShellMariadbTableTreeItem(table, this.getTreeView()));
                            }
                            this.setChild(list);
                        } else {// 有数据则执行删除、新增、更新操作
                            ObservableList children = this.richChildren();
                            ObservableList<ShellMariadbTableTreeItem> list = children;
                            List<ShellMariadbTableTreeItem> delList = new ArrayList<>();
                            List<ShellMariadbTableTreeItem> addList = new ArrayList<>();
                            // 删除
                            for (ShellMariadbTableTreeItem item : list) {
                                if (tables.parallelStream().noneMatch(f -> f.compare(item.value()))) {
                                    delList.add(item);
                                }
                            }
                            // 新增
                            for (MariadbTable table : tables) {
                                if (list.parallelStream().noneMatch(item -> table.compare(item.value()))) {
                                    addList.add(new ShellMariadbTableTreeItem(table, this.getTreeView()));
                                }
                            }
                            // 更新
                            for (ShellMariadbTableTreeItem item : list) {
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
                        MessageBox.exception(ex);
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
     * 获取数据库名称
     *
     * @return 数据库名称
     */
    public String dbName() {
        return this.parent().dbName();
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
     * 表数量缓存
     */
    private Integer tableSize;

    /**
     * 获取表数量（带缓存）
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
     * 获取MariaDB信息
     *
     * @return MariaDB信息
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
     * 新增表并刷新排序
     *
     * @param table 表对象
     */
    public void addTable(MariadbTable table) {
        this.addChild(new ShellMariadbTableTreeItem(table, this.getTreeView()));
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
